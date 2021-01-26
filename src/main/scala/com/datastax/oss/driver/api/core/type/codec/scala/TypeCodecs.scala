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

package com.datastax.oss.driver.api.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.internal.core.`type`.codec.scala._

import scala.collection.immutable.{ SortedMap, SortedSet }

/** Constants and factory methods to obtain instances of the Scala type codecs.
 * These codecs have to be registered manually
 * See [[com.datastax.oss.driver.api.core.`type`.codec.registry.MutableCodecRegistry]]
 */
object TypeCodecs {

  /** The default codec that maps CQL type Decimal to Scala's [[BigDecimal]] */
  val bigDecimalCodec: TypeCodec[BigDecimal] = BigDecimalCodec

  /** The default codec that maps CQL type BigInt to Scala's [[BigInt]] */
  val bigIntCodec: TypeCodec[BigInt] = BigIntCodec

  /** The default codec that maps CQL type Boolean to Scala's [[Boolean]] */
  val booleanCodec: TypeCodec[Boolean] = BooleanCodec

  /** The default codec that maps CQL type TinyInt to Scala's [[BigInt]] */
  val byteCodec: TypeCodec[Byte] = ByteCodec

  /** The default codec that maps CQL type Double to Scala's [[Double]] */
  val doubleCodec: TypeCodec[Double] = DoubleCodec

  /** The default codec that maps CQL type Float to Scala's [[Float]] */
  val floatCodec: TypeCodec[Float] = FloatCodec

  /** The default codec that maps CQL type Int to Scala's [[Int]] */
  val intCodec: TypeCodec[Int] = IntCodec

  /** The default codec that maps CQL type BigInt to Scala's [[Long]] */
  val longCodec: TypeCodec[Long] = LongCodec

  /** The default codec that maps CQL type SmallInt to Scala's [[Short]] */
  val shortCodec: TypeCodec[Short] = ShortCodec

  /** The default codec that maps CQL type Text to Scala's [[String]] */
  val stringCodec: TypeCodec[String] = com.datastax.oss.driver.api.core.`type`.codec.TypeCodecs.TEXT

  /**
   * Builds a new codec that wraps another codec's type into [[Option]] instances
   * (mapping CQL null to [[None]]).
   */
  def optionOf[T](inner: TypeCodec[T]): TypeCodec[Option[T]] = OptionCodec(inner)

  /** Builds a new codec that maps a CQL list to a Scala Seq, using the given codec to map each
    * element.
    */
  def seqOf[T](inner: TypeCodec[T]): TypeCodec[Seq[T]] = SeqCodec.frozen(inner)

  /** Builds a new codec that maps a CQL list to a Scala List, using the given codec to map each
    * element.
    */
  def listOf[T](inner: TypeCodec[T]): TypeCodec[List[T]] = ListCodec.frozen(inner)

  /** Builds a new codec that maps a CQL list to a Scala Vector, using the given codec to map each
    * element.
    */
  def vectorOf[T](inner: TypeCodec[T]): TypeCodec[Vector[T]] = VectorCodec.frozen(inner)

  /** Builds a new codec that maps a CQL set to a Scala Set, using the given codec to map each
    * element.
    */
  def setOf[T](inner: TypeCodec[T]): TypeCodec[Set[T]] = SetCodec.frozen(inner)

  /** Builds a new codec that maps a CQL set to a Scala SortedSet, using the given codec to map each
    * element.
    */
  def sortedSetOf[T: Ordering](inner: TypeCodec[T]): TypeCodec[SortedSet[T]] =
    SortedSetCodec.frozen(inner)

  /** Builds a new codec that maps a CQL map to a Scala map, using the given codecs to map each key
    * and value.
    */
  def mapOf[K, V](keyInner: TypeCodec[K], valueInner: TypeCodec[V]): TypeCodec[Map[K, V]] =
    MapCodec.frozen(keyInner, valueInner)

  /** Builds a new codec that maps a CQL map to a Scala sorted map, using the given codecs to map each key
    * and value.
    */
  def sorterMapOf[K: Ordering, V](
      keyInner: TypeCodec[K],
      valueInner: TypeCodec[V]
  ): TypeCodec[SortedMap[K, V]] =
    SortedMapCodec.frozen(keyInner, valueInner)

  // **********************************************************************
  // To generate methods to Tuple2 and above, use this template method.
  // **********************************************************************
  //
  //  def template(t: Int): String = {
  //    val ts             = (1 to t).map(i => s"t$i")
  //    val Ts             = ts.map(_.toUpperCase)
  //    val typeParameters = Ts.mkString(", ")
  //    val parameters = ts.zip(Ts).map {
  //      case (param, typeParameter) => s"$param: TypeCodec[$typeParameter]"
  //    }.mkString(", ")
  //
  //    s"""/**
  //       | * Builds a new codec that maps a CQL tuple to a Scala Tuple$t, for the given type
  //       | * definition.
  //       | */
  //       |def tuple${t}Of[$typeParameters]($parameters): TypeCodec[($typeParameters)] =
  //       |    TupleCodec.tuple${t}[$typeParameters](${ts.mkString(", ")})
  //       |""".stripMargin
  //  }
  //
  //  (2 to 22).map(template).foreach(println(_))

  // format: off

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple1, for the given type
   * definition.
   */
  def tuple1Of[T](t1: TypeCodec[T]): TypeCodec[Tuple1[T]] =
    TupleCodec.tuple1[T](t1)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple2, for the given type
   * definition.
   */
  def tuple2Of[T1, T2](t1: TypeCodec[T1], t2: TypeCodec[T2]): TypeCodec[(T1, T2)] =
    TupleCodec.tuple2[T1, T2](t1, t2)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple3, for the given type
   * definition.
   */
  def tuple3Of[T1, T2, T3](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3]): TypeCodec[(T1, T2, T3)] =
    TupleCodec.tuple3[T1, T2, T3](t1, t2, t3)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple4, for the given type
   * definition.
   */
  def tuple4Of[T1, T2, T3, T4](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4]): TypeCodec[(T1, T2, T3, T4)] =
    TupleCodec.tuple4[T1, T2, T3, T4](t1, t2, t3, t4)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple5, for the given type
   * definition.
   */
  def tuple5Of[T1, T2, T3, T4, T5](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5]): TypeCodec[(T1, T2, T3, T4, T5)] =
    TupleCodec.tuple5[T1, T2, T3, T4, T5](t1, t2, t3, t4, t5)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple6, for the given type
   * definition.
   */
  def tuple6Of[T1, T2, T3, T4, T5, T6](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6]): TypeCodec[(T1, T2, T3, T4, T5, T6)] =
    TupleCodec.tuple6[T1, T2, T3, T4, T5, T6](t1, t2, t3, t4, t5, t6)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple7, for the given type
   * definition.
   */
  def tuple7Of[T1, T2, T3, T4, T5, T6, T7](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7)] =
    TupleCodec.tuple7[T1, T2, T3, T4, T5, T6, T7](t1, t2, t3, t4, t5, t6, t7)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple8, for the given type
   * definition.
   */
  def tuple8Of[T1, T2, T3, T4, T5, T6, T7, T8](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8)] =
    TupleCodec.tuple8[T1, T2, T3, T4, T5, T6, T7, T8](t1, t2, t3, t4, t5, t6, t7, t8)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple9, for the given type
   * definition.
   */
  def tuple9Of[T1, T2, T3, T4, T5, T6, T7, T8, T9](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9)] =
    TupleCodec.tuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9](t1, t2, t3, t4, t5, t6, t7, t8, t9)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple10, for the given type
   * definition.
   */
  def tuple10Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)] =
    TupleCodec.tuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple11, for the given type
   * definition.
   */
  def tuple11Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)] =
    TupleCodec.tuple11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple12, for the given type
   * definition.
   */
  def tuple12Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)] =
    TupleCodec.tuple12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple13, for the given type
   * definition.
   */
  def tuple13Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)] =
    TupleCodec.tuple13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple14, for the given type
   * definition.
   */
  def tuple14Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)] =
    TupleCodec.tuple14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple15, for the given type
   * definition.
   */
  def tuple15Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)] =
    TupleCodec.tuple15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple16, for the given type
   * definition.
   */
  def tuple16Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)] =
    TupleCodec.tuple16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple17, for the given type
   * definition.
   */
  def tuple17Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)] =
    TupleCodec.tuple17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple18, for the given type
   * definition.
   */
  def tuple18Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)] =
    TupleCodec.tuple18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple19, for the given type
   * definition.
   */
  def tuple19Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)] =
    TupleCodec.tuple19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple20, for the given type
   * definition.
   */
  def tuple20Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)] =
    TupleCodec.tuple20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple21, for the given type
   * definition.
   */
  def tuple21Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20], t21: TypeCodec[T21]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)] =
    TupleCodec.tuple21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21)

  /**
   * Builds a new codec that maps a CQL tuple to a Scala Tuple22, for the given type
   * definition.
   */
  def tuple22Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22](t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20], t21: TypeCodec[T21], t22: TypeCodec[T22]): TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)] =
    TupleCodec.tuple22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22](t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22)

  // format: on
}
