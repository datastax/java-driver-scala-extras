/*
 * Copyright 2017 DataStax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datastax.oss.driver.internal.core.`type`.codec.scala

import java.nio.{ BufferUnderflowException, ByteBuffer }

import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.DataType
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.{ GenericType, GenericTypeParameter }
import com.datastax.oss.driver.internal.core.`type`.DefaultTupleType
import com.datastax.oss.driver.internal.core.`type`.codec.ParseUtils
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken

/** Offers methods for creating TupleCodecs.
  * This is not a type safe implementation like the other codecs, while maintaining a typesafe API.
  * The problem mainly comes from inability of building tuples piece by piece in a type safe manner.
  */
object TupleCodec {

  // This is similar to collections, but doesn't store the total size of thee collection
  def pack(buffers: Array[ByteBuffer], version: ProtocolVersion): ByteBuffer = {
    var size = 0
    for (idx <- buffers.indices) {
      size += sizeOfValue(buffers(idx), version)
    }

    val result = ByteBuffer.allocate(size)
    for (idx <- buffers.indices) {
      writeValue(result, buffers(idx), version)
    }

    result.flip().asInstanceOf[ByteBuffer]
  }

  private class UnsafeTupleCodec[T <: Product](
      codecs: List[TypeCodec[_]],
      curriedConstructor: _ => _,
      override val getJavaType: GenericType[T]
  ) extends TypeCodec[T] {

    import scala.jdk.CollectionConverters._

    private val codecsSize = codecs.size

    override val getCqlType: DataType = new DefaultTupleType(codecs.map(_.getCqlType).asJava)

    override def encode(tuple: T, protocolVersion: ProtocolVersion): ByteBuffer =
      if (tuple == null) null
      else {
        require(tuple.productArity == codecs.size, s"Tuple must have size [$codecsSize]")

        val buffers = List.newBuilder[ByteBuffer]
        tuple.productIterator.toList.zip(codecs.asInstanceOf[List[TypeCodec[Any]]]).foreach {
          case (value, codec) =>
            buffers += codec
              .encode(value, protocolVersion) // Could double check here and call 'accept'
        }

        TupleCodec.pack(buffers.result().toArray, protocolVersion)
      }

    override def decode(bytes: ByteBuffer, protocolVersion: ProtocolVersion): T =
      if (bytes == null) null.asInstanceOf[T]
      else {
        val input = bytes.duplicate()
        var const = curriedConstructor.asInstanceOf[Any => Any]
        var codec = codecs

        try {
          while (codec.nonEmpty) {
            val elementSize = input.getInt
            val element: Any = if (elementSize < 0) {
              null
            } else {
              val element = input.slice()
              element.limit(elementSize)
              input.position(input.position() + elementSize)

              codec.head.decode(element, protocolVersion)
            }

            const(element) match {
              case f: Function1[_, _] =>
                const = f.asInstanceOf[Any => Any]
                codec = codec.tail
              case tuple =>
                return tuple.asInstanceOf[T]
            }
          }

          throw new IllegalArgumentException(
            s"Too many fields in encoded tuple, expected [${codecs.size}]"
          )
        } catch {
          case e: BufferUnderflowException =>
            throw new IllegalArgumentException("Not enough bytes to deserialize a tuple", e);
        }
      }

    override def format(value: T): String =
      if (value == null) "NULL"
      else {
        require(value.productArity == codecs.size, s"Tuple must have size [$codecsSize]")

        value.productIterator.toList
          .zip(codecs.asInstanceOf[List[TypeCodec[Any]]])
          .map { case (v, codec) =>
            codec.format(v)
          }
          .mkString("(", ",", ")")
      }

    override def parse(value: String): T =
      if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) {
        null.asInstanceOf[T]
      } else {
        var idx   = ParseUtils.skipSpaces(value, 0)
        var const = curriedConstructor.asInstanceOf[Any => Any]
        var codec = codecs

        if (value.charAt(idx) != '(') {
          throw new IllegalArgumentException(
            s"Cannot parse tuple value from '$value', at character $idx expecting '(' but got '${value
              .charAt(idx)}''"
          )
        }

        idx = ParseUtils.skipSpaces(value, idx + 1)
        while (idx < value.length) {
          if (codec.isEmpty)
            throw new IllegalArgumentException(
              s"Cannot parse tuple from '$value', more values than expected"
            )

          val n      = ParseUtils.skipCQLValue(value, idx)
          val parsed = codec.head.parse(value.substring(idx, n))
          idx = ParseUtils.skipSpaces(value, n)

          if (idx >= value.length) {
            throw new IllegalArgumentException(
              s"Malformed tuple value '$value', missing closing ')'"
            )
          }

          const(parsed) match {
            case f: Function1[_, _] =>
              if (value.charAt(idx) != ',') {
                throw new IllegalArgumentException(
                  s"Cannot parse tuple value from '$value', at character $idx expecting ',' but got '${value
                    .charAt(idx)}''"
                )
              }

              const = f.asInstanceOf[Any => Any]
              codec = codec.tail
              idx   = ParseUtils.skipSpaces(value, idx + 1)
            case tuple =>
              if (value.charAt(idx) != ')') {
                throw new IllegalArgumentException(
                  s"Malformed tuple value '$value', expected closing ')' but got '${value.charAt(idx)}'"
                )
              }

              return tuple.asInstanceOf[T]
          }
        }
        throw new IllegalArgumentException(s"Malformed tuple value '$value', missing closing ')'")
      }

    override def accepts(value: Any): Boolean = value match {
      case tuple: Product if tuple.productArity == codecsSize =>
        tuple.productIterator.toList.zip(codecs.asInstanceOf[List[TypeCodec[Any]]]).forall {
          case (value, codec) => codec.accepts(value)
        }
      case _ => false
    }
  }

  def tuple1[T1](t1: TypeCodec[T1]): TypeCodec[Tuple1[T1]] =
    new UnsafeTupleCodec[Tuple1[T1]](
      List(t1),
      Tuple1(t1).copy[T1] _,
      GenericType
        .of(new TypeToken[Tuple1[T1]]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .asInstanceOf[GenericType[Tuple1[T1]]]
    )

  // **********************************************************************
  // To generate methods to Tuple2 and above, use this template method.
  // **********************************************************************
  //
  // def template(t: Int): String = {
  //    val ts             = (1 to t).map(i => s"t$i")
  //    val Ts             = ts.map(_.toUpperCase)
  //    val typeParameters = Ts.mkString(", ")
  //    val tupleType      = s"(${typeParameters})"
  //
  //    s"""def tuple${t}[${typeParameters}](
  //       |  ${ts.map(t => s"${t}: TypeCodec[${t.toUpperCase}]").mkString(", ")}
  //       |): TypeCodec[$tupleType] =
  //       |  new UnsafeTupleCodec[$tupleType](
  //       |    List(${ts.mkString(", ")}),
  //       |    ((${ts.mkString(", ")})
  //       |        .copy[${typeParameters}] _).curried,
  //       |    GenericType
  //       |        .of(new TypeToken[${tupleType}]() {}.getType)
  //       |        ${ts
  //      .map(t => s".where(new GenericTypeParameter[${t.toUpperCase}] {}, ${t}.getJavaType.wrap())")
  //      .mkString("\n")}
  //       |        .asInstanceOf[GenericType[${tupleType}]]
  //       |  )
  //       |""".stripMargin
  //  }
  //
  //  (2 to 22).map(template).foreach(println(_))

  // format: off

  def tuple2[T1, T2](
    t1: TypeCodec[T1], t2: TypeCodec[T2]
  ): TypeCodec[(T1, T2)] =
    new UnsafeTupleCodec[(T1, T2)](
      List(t1, t2),
      ((t1, t2)
        .copy[T1, T2] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2)]]
    )



  def tuple3[T1, T2, T3](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3]
  ): TypeCodec[(T1, T2, T3)] =
    new UnsafeTupleCodec[(T1, T2, T3)](
      List(t1, t2, t3),
      ((t1, t2, t3)
        .copy[T1, T2, T3] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3)]]
    )



  def tuple4[T1, T2, T3, T4](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4]
  ): TypeCodec[(T1, T2, T3, T4)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4)](
      List(t1, t2, t3, t4),
      ((t1, t2, t3, t4)
        .copy[T1, T2, T3, T4] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4)]]
    )



  def tuple5[T1, T2, T3, T4, T5](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5]
  ): TypeCodec[(T1, T2, T3, T4, T5)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5)](
      List(t1, t2, t3, t4, t5),
      ((t1, t2, t3, t4, t5)
        .copy[T1, T2, T3, T4, T5] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5)]]
    )



  def tuple6[T1, T2, T3, T4, T5, T6](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6)](
      List(t1, t2, t3, t4, t5, t6),
      ((t1, t2, t3, t4, t5, t6)
        .copy[T1, T2, T3, T4, T5, T6] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6)]]
    )



  def tuple7[T1, T2, T3, T4, T5, T6, T7](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7)](
      List(t1, t2, t3, t4, t5, t6, t7),
      ((t1, t2, t3, t4, t5, t6, t7)
        .copy[T1, T2, T3, T4, T5, T6, T7] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7)]]
    )



  def tuple8[T1, T2, T3, T4, T5, T6, T7, T8](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8)](
      List(t1, t2, t3, t4, t5, t6, t7, t8),
      ((t1, t2, t3, t4, t5, t6, t7, t8)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8)]]
    )



  def tuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9)]]
    )



  def tuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)]]
    )



  def tuple11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)]]
    )



  def tuple12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .where(new GenericTypeParameter[T12] {}, t12.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)]]
    )



  def tuple13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .where(new GenericTypeParameter[T12] {}, t12.getJavaType.wrap())
        .where(new GenericTypeParameter[T13] {}, t13.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)]]
    )



  def tuple14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .where(new GenericTypeParameter[T12] {}, t12.getJavaType.wrap())
        .where(new GenericTypeParameter[T13] {}, t13.getJavaType.wrap())
        .where(new GenericTypeParameter[T14] {}, t14.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)]]
    )



  def tuple15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .where(new GenericTypeParameter[T12] {}, t12.getJavaType.wrap())
        .where(new GenericTypeParameter[T13] {}, t13.getJavaType.wrap())
        .where(new GenericTypeParameter[T14] {}, t14.getJavaType.wrap())
        .where(new GenericTypeParameter[T15] {}, t15.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)]]
    )



  def tuple16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .where(new GenericTypeParameter[T12] {}, t12.getJavaType.wrap())
        .where(new GenericTypeParameter[T13] {}, t13.getJavaType.wrap())
        .where(new GenericTypeParameter[T14] {}, t14.getJavaType.wrap())
        .where(new GenericTypeParameter[T15] {}, t15.getJavaType.wrap())
        .where(new GenericTypeParameter[T16] {}, t16.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)]]
    )



  def tuple17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .where(new GenericTypeParameter[T12] {}, t12.getJavaType.wrap())
        .where(new GenericTypeParameter[T13] {}, t13.getJavaType.wrap())
        .where(new GenericTypeParameter[T14] {}, t14.getJavaType.wrap())
        .where(new GenericTypeParameter[T15] {}, t15.getJavaType.wrap())
        .where(new GenericTypeParameter[T16] {}, t16.getJavaType.wrap())
        .where(new GenericTypeParameter[T17] {}, t17.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)]]
    )



  def tuple18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .where(new GenericTypeParameter[T12] {}, t12.getJavaType.wrap())
        .where(new GenericTypeParameter[T13] {}, t13.getJavaType.wrap())
        .where(new GenericTypeParameter[T14] {}, t14.getJavaType.wrap())
        .where(new GenericTypeParameter[T15] {}, t15.getJavaType.wrap())
        .where(new GenericTypeParameter[T16] {}, t16.getJavaType.wrap())
        .where(new GenericTypeParameter[T17] {}, t17.getJavaType.wrap())
        .where(new GenericTypeParameter[T18] {}, t18.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)]]
    )



  def tuple19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](
     t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19]
   ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .where(new GenericTypeParameter[T12] {}, t12.getJavaType.wrap())
        .where(new GenericTypeParameter[T13] {}, t13.getJavaType.wrap())
        .where(new GenericTypeParameter[T14] {}, t14.getJavaType.wrap())
        .where(new GenericTypeParameter[T15] {}, t15.getJavaType.wrap())
        .where(new GenericTypeParameter[T16] {}, t16.getJavaType.wrap())
        .where(new GenericTypeParameter[T17] {}, t17.getJavaType.wrap())
        .where(new GenericTypeParameter[T18] {}, t18.getJavaType.wrap())
        .where(new GenericTypeParameter[T19] {}, t19.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)]]
    )



  def tuple20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .where(new GenericTypeParameter[T12] {}, t12.getJavaType.wrap())
        .where(new GenericTypeParameter[T13] {}, t13.getJavaType.wrap())
        .where(new GenericTypeParameter[T14] {}, t14.getJavaType.wrap())
        .where(new GenericTypeParameter[T15] {}, t15.getJavaType.wrap())
        .where(new GenericTypeParameter[T16] {}, t16.getJavaType.wrap())
        .where(new GenericTypeParameter[T17] {}, t17.getJavaType.wrap())
        .where(new GenericTypeParameter[T18] {}, t18.getJavaType.wrap())
        .where(new GenericTypeParameter[T19] {}, t19.getJavaType.wrap())
        .where(new GenericTypeParameter[T20] {}, t20.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)]]
    )



  def tuple21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20], t21: TypeCodec[T21]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .where(new GenericTypeParameter[T12] {}, t12.getJavaType.wrap())
        .where(new GenericTypeParameter[T13] {}, t13.getJavaType.wrap())
        .where(new GenericTypeParameter[T14] {}, t14.getJavaType.wrap())
        .where(new GenericTypeParameter[T15] {}, t15.getJavaType.wrap())
        .where(new GenericTypeParameter[T16] {}, t16.getJavaType.wrap())
        .where(new GenericTypeParameter[T17] {}, t17.getJavaType.wrap())
        .where(new GenericTypeParameter[T18] {}, t18.getJavaType.wrap())
        .where(new GenericTypeParameter[T19] {}, t19.getJavaType.wrap())
        .where(new GenericTypeParameter[T20] {}, t20.getJavaType.wrap())
        .where(new GenericTypeParameter[T21] {}, t21.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)]]
    )



  def tuple22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22](
    t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20], t21: TypeCodec[T21], t22: TypeCodec[T22]
  ): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)] =
    new UnsafeTupleCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)](
      List(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22),
      ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22)
        .copy[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22] _).curried,
      GenericType
        .of(new TypeToken[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)]() {}.getType)
        .where(new GenericTypeParameter[T1] {}, t1.getJavaType.wrap())
        .where(new GenericTypeParameter[T2] {}, t2.getJavaType.wrap())
        .where(new GenericTypeParameter[T3] {}, t3.getJavaType.wrap())
        .where(new GenericTypeParameter[T4] {}, t4.getJavaType.wrap())
        .where(new GenericTypeParameter[T5] {}, t5.getJavaType.wrap())
        .where(new GenericTypeParameter[T6] {}, t6.getJavaType.wrap())
        .where(new GenericTypeParameter[T7] {}, t7.getJavaType.wrap())
        .where(new GenericTypeParameter[T8] {}, t8.getJavaType.wrap())
        .where(new GenericTypeParameter[T9] {}, t9.getJavaType.wrap())
        .where(new GenericTypeParameter[T10] {}, t10.getJavaType.wrap())
        .where(new GenericTypeParameter[T11] {}, t11.getJavaType.wrap())
        .where(new GenericTypeParameter[T12] {}, t12.getJavaType.wrap())
        .where(new GenericTypeParameter[T13] {}, t13.getJavaType.wrap())
        .where(new GenericTypeParameter[T14] {}, t14.getJavaType.wrap())
        .where(new GenericTypeParameter[T15] {}, t15.getJavaType.wrap())
        .where(new GenericTypeParameter[T16] {}, t16.getJavaType.wrap())
        .where(new GenericTypeParameter[T17] {}, t17.getJavaType.wrap())
        .where(new GenericTypeParameter[T18] {}, t18.getJavaType.wrap())
        .where(new GenericTypeParameter[T19] {}, t19.getJavaType.wrap())
        .where(new GenericTypeParameter[T20] {}, t20.getJavaType.wrap())
        .where(new GenericTypeParameter[T21] {}, t21.getJavaType.wrap())
        .where(new GenericTypeParameter[T22] {}, t22.getJavaType.wrap())
        .asInstanceOf[GenericType[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)]]
    )
  // format: on
}
