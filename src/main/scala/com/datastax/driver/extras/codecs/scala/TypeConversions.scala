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

package com.datastax.driver.extras.codecs.scala

import java.lang.reflect.{GenericArrayType, ParameterizedType, Type => JType}
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import com.datastax.driver.core.exceptions.CodecNotFoundException
import com.datastax.driver.core.{Duration, TypeCodec}
import com.datastax.driver.extras.codecs.jdk8.{InstantCodec, LocalTimeCodec}
import com.google.common.reflect.TypeToken

import scala.reflect.runtime.universe._

object TypeConversions {

  private val mirror = runtimeMirror(getClass.getClassLoader)

  def toCodec[T](tpe: Type): TypeCodec[T] = {

    val codec = tpe match {

      case t if t =:= typeOf[Boolean] => BooleanCodec
      case t if t =:= typeOf[Byte] => ByteCodec
      case t if t =:= typeOf[Short] => ShortCodec
      case t if t =:= typeOf[Int] => IntCodec
      case t if t =:= typeOf[Long] => LongCodec
      case t if t =:= typeOf[Float] => FloatCodec
      case t if t =:= typeOf[Double] => DoubleCodec

      case t if t =:= typeOf[BigInt] => BigIntCodec
      case t if t =:= typeOf[BigDecimal] => BigDecimalCodec2

      case t if t =:= typeOf[String] => TypeCodec.varchar()
      case t if t =:= typeOf[ByteBuffer] => TypeCodec.blob()
      case t if t =:= typeOf[Date] => TypeCodec.timestamp()
      case t if t =:= typeOf[Duration] => TypeCodec.duration()
      case t if t =:= typeOf[com.datastax.driver.core.LocalDate] => TypeCodec.date()
      case t if t =:= typeOf[InetAddress] => TypeCodec.inet()
      case t if t =:= typeOf[UUID] => TypeCodec.uuid()

      case t if t =:= typeOf[java.time.Instant] => InstantCodec.instance
      case t if t =:= typeOf[java.time.LocalDate] => LocalDateCodec
      case t if t =:= typeOf[java.time.LocalTime] => LocalTimeCodec.instance
      case t if t =:= typeOf[java.time.LocalDateTime] => LocalDateTimeCodec

      case t if t <:< typeOf[Option[_]] => OptionCodec(toCodec[Any](tpe.typeArgs.head))

      case t if t <:< typeOf[Seq[_]] => SeqCodec(toCodec[Any](tpe.typeArgs.head))
      case t if t <:< typeOf[Set[_]] => SetCodec(toCodec[Any](tpe.typeArgs.head))
      case t if t <:< typeOf[Map[_, _]] => MapCodec(toCodec[Any](tpe.typeArgs.head), toCodec[Any](tpe.typeArgs(1)))

      case t if t <:< typeOf[(_, _)] => Tuple2Codec(toCodec[Any](tpe.typeArgs.head), toCodec[Any](tpe.typeArgs(1)))
      case t if t <:< typeOf[(_, _, _)] => Tuple3Codec(toCodec[Any](tpe.typeArgs.head), toCodec[Any](tpe.typeArgs(1)), toCodec[Any](tpe.typeArgs(2)))

      case _ =>
        val javaType = TypeToken.of(mirror.runtimeClass(tpe.typeSymbol.asClass))
        throw new CodecNotFoundException(
          s"""Codec not found for requested operation: [ANY <-> ${tpe.typeSymbol}]""", null, javaType)

    }

    codec.asInstanceOf[TypeCodec[T]]

  }

  def toJavaType(tpe: Type): JType = tpe match {

    // primitive types need to be wrapped
    case t if t =:= typeOf[Boolean] => classOf[java.lang.Boolean]
    case t if t =:= typeOf[Byte] => classOf[java.lang.Byte]
    case t if t =:= typeOf[Short] => classOf[java.lang.Short]
    case t if t =:= typeOf[Int] => classOf[java.lang.Integer]
    case t if t =:= typeOf[Long] => classOf[java.lang.Long]
    case t if t =:= typeOf[Float] => classOf[java.lang.Float]
    case t if t =:= typeOf[Double] => classOf[java.lang.Double]

    case TypeRef(_, _, args) =>
      val sym = tpe.typeSymbol
      if (args.isEmpty) {
        mirror.runtimeClass(sym.asClass)
      } else if (sym == symbolOf[Array[_]]) {
        new ScalaGenericArrayType(mirror.runtimeClass(args.head.typeSymbol.asClass))
      } else {
        new ScalaParameterizedType(mirror.runtimeClass(sym.asClass), args.map(toJavaType).toArray, null)
      }
  }

  private class ScalaParameterizedType(val rawType: Class[_], val actualTypeArguments: Array[JType], val ownerType: JType) extends ParameterizedType {

    override def getRawType: Class[_] = rawType

    override def getActualTypeArguments: Array[JType] = actualTypeArguments

    override def getOwnerType: JType = ownerType

    override def equals(o: Any): Boolean = o match {
      case that: ParameterizedType =>
        if (this eq that) true
        else {
          val thatOwner = that.getOwnerType
          val thatRawType = that.getRawType
          if (this.ownerType == null) if (thatOwner != null) false
          else if (!(this.ownerType == thatOwner)) false
          if (this.rawType == null) if (thatRawType != null) false
          else if (!(this.rawType == thatRawType)) false
          this.actualTypeArguments sameElements that.getActualTypeArguments
        }
      case _ => false
    }

    override def hashCode: Int = actualTypeArguments.toSeq.hashCode ^
      (if (this.ownerType == null) 0 else this.ownerType.hashCode) ^
      (if (this.rawType == null) 0 else this.rawType.hashCode)

    override def toString: String = {
      val sb: StringBuilder = new StringBuilder
      if (this.ownerType != null) {
        this.ownerType match {
          case clazz: Class[_] => sb.append(clazz.getName)
          case _ => sb.append(this.ownerType.toString)
        }
        sb.append(".")
        sb.append(this.rawType.getName)
      }
      else sb.append(this.rawType.getName)
      if (this.actualTypeArguments != null && this.actualTypeArguments.length > 0) {
        sb.append("<")
        val args = this.actualTypeArguments
        val length = args.length
        var first = true
        var n: Int = 0
        while (n < length) {
          val t = args(n)
          if (!first) sb.append(", ")
          t match {
            case clazz: Class[_] => sb.append(clazz.getName)
            case _ => sb.append(t.toString)
          }
          first = false
          n += 1
        }
        sb.append(">")
      }
      sb.toString
    }
  }

  private class ScalaGenericArrayType(val genericComponentType: JType) extends GenericArrayType {

    override def getGenericComponentType: JType = genericComponentType

    override def toString: String = {
      val componentType = this.genericComponentType
      val sb = new StringBuilder
      componentType match {
        case clazz: Class[_] => sb.append(clazz.getName)
        case _ => sb.append(componentType.toString)
      }
      sb.append("[]")
      sb.toString
    }

    override def equals(o: Any): Boolean = o match {
      case that: GenericArrayType =>
        val thatComponentType = that.getGenericComponentType
        this.genericComponentType == thatComponentType
      case _ => false
    }

    override def hashCode: Int = this.genericComponentType.hashCode

  }

}
