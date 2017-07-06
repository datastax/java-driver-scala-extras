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

import java.nio.ByteBuffer

import com.datastax.driver.core.TypeCodec.AbstractTupleCodec
import com.datastax.driver.core._
import com.datastax.driver.extras.codecs.scala.Implicits._
import com.google.common.reflect.TypeToken

class Tuple3Codec[T1, T2, T3](
    cqlType: TupleType, javaType: TypeToken[(T1, T2, T3)],
    eltCodecs: (TypeCodec[T1], TypeCodec[T2], TypeCodec[T3]))
  extends AbstractTupleCodec[(T1, T2, T3)](cqlType, javaType)
    with VersionAgnostic[(T1, T2, T3)] {

  def this(eltCodec1: TypeCodec[T1], eltCodec2: TypeCodec[T2], eltCodec3: TypeCodec[T3])(implicit protocolVersion: ProtocolVersion, codecRegistry: CodecRegistry) {
    this(
      TupleType.of(protocolVersion, codecRegistry, eltCodec1.getCqlType, eltCodec2.getCqlType, eltCodec3.getCqlType),
      TypeTokens.tuple3Of(eltCodec1.getJavaType, eltCodec2.getJavaType, eltCodec3.getJavaType),
      (eltCodec1, eltCodec2, eltCodec3)
    )
  }

  {
    val componentTypes = cqlType.getComponentTypes
    require(componentTypes.size() == 3, s"Expecting TupleType with 3 components, got ${componentTypes.size()}")
    require(eltCodecs._1.accepts(componentTypes.get(0)), s"Codec for component 1 does not accept component type: ${componentTypes.get(0)}")
    require(eltCodecs._2.accepts(componentTypes.get(1)), s"Codec for component 2 does not accept component type: ${componentTypes.get(1)}")
    require(eltCodecs._3.accepts(componentTypes.get(2)), s"Codec for component 3 does not accept component type: ${componentTypes.get(2)}")
  }

  override protected def newInstance(): (T1, T2, T3) = null

  override protected def serializeField(source: (T1, T2, T3), index: Int, protocolVersion: ProtocolVersion): ByteBuffer = index match {
    case 0 => eltCodecs._1.serialize(source._1, protocolVersion)
    case 1 => eltCodecs._2.serialize(source._2, protocolVersion)
    case 2 => eltCodecs._3.serialize(source._3, protocolVersion)
  }

  override protected def deserializeAndSetField(input: ByteBuffer, target: (T1, T2, T3), index: Int, protocolVersion: ProtocolVersion): (T1, T2, T3) = index match {
    case 0 => Tuple3(eltCodecs._1.deserialize(input, protocolVersion), null.asInstanceOf[T2], null.asInstanceOf[T3])
    case 1 => target.copy(_2 = eltCodecs._2.deserialize(input, protocolVersion))
    case 2 => target.copy(_3 = eltCodecs._3.deserialize(input, protocolVersion))
  }

  override protected def formatField(source: (T1, T2, T3), index: Int): String = index match {
    case 0 => eltCodecs._1.format(source._1)
    case 1 => eltCodecs._2.format(source._2)
    case 2 => eltCodecs._3.format(source._3)
  }

  override protected def parseAndSetField(input: String, target: (T1, T2, T3), index: Int): (T1, T2, T3) = index match {
    case 0 => Tuple3(eltCodecs._1.parse(input), null.asInstanceOf[T2], null.asInstanceOf[T3])
    case 1 => target.copy(_2 = eltCodecs._2.parse(input))
    case 2 => target.copy(_3 = eltCodecs._3.parse(input))
  }

}

object Tuple3Codec {

  def apply[T1, T2, T3](eltCodec1: TypeCodec[T1], eltCodec2: TypeCodec[T2], eltCodec3: TypeCodec[T3]): Tuple3Codec[T1, T2, T3] =
    new Tuple3Codec[T1, T2, T3](eltCodec1, eltCodec2, eltCodec3)

  import scala.reflect.runtime.universe._

  def apply[T1, T2, T3](implicit eltTag1: TypeTag[T1], eltTag2: TypeTag[T2], eltTag3: TypeTag[T3]): Tuple3Codec[T1, T2, T3] = {
    val eltCodec1 = TypeConversions.toCodec[T1](eltTag1.tpe)
    val eltCodec2 = TypeConversions.toCodec[T2](eltTag2.tpe)
    val eltCodec3 = TypeConversions.toCodec[T3](eltTag3.tpe)
    apply(eltCodec1, eltCodec2, eltCodec3)
  }

}

