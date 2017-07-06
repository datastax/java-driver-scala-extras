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

class Tuple2Codec[T1, T2](
    cqlType: TupleType, javaType: TypeToken[(T1, T2)],
    eltCodecs: (TypeCodec[T1], TypeCodec[T2]))
  extends AbstractTupleCodec[(T1, T2)](cqlType, javaType)
    with VersionAgnostic[(T1, T2)] {

  def this(eltCodec1: TypeCodec[T1], eltCodec2: TypeCodec[T2])(implicit protocolVersion: ProtocolVersion, codecRegistry: CodecRegistry) {
    this(
      TupleType.of(protocolVersion, codecRegistry, eltCodec1.getCqlType, eltCodec2.getCqlType),
      TypeTokens.tuple2Of(eltCodec1.getJavaType, eltCodec2.getJavaType),
      (eltCodec1, eltCodec2)
    )
  }

  {
    val componentTypes = cqlType.getComponentTypes
    require(componentTypes.size() == 2, s"Expecting TupleType with 2 components, got ${componentTypes.size()}")
    require(eltCodecs._1.accepts(componentTypes.get(0)), s"Codec for component 1 does not accept component type: ${componentTypes.get(0)}")
    require(eltCodecs._2.accepts(componentTypes.get(1)), s"Codec for component 2 does not accept component type: ${componentTypes.get(1)}")
  }

  override protected def newInstance(): (T1, T2) = null

  override protected def serializeField(source: (T1, T2), index: Int, protocolVersion: ProtocolVersion): ByteBuffer = index match {
    case 0 => eltCodecs._1.serialize(source._1, protocolVersion)
    case 1 => eltCodecs._2.serialize(source._2, protocolVersion)
  }

  override protected def deserializeAndSetField(input: ByteBuffer, target: (T1, T2), index: Int, protocolVersion: ProtocolVersion): (T1, T2) = index match {
    case 0 => Tuple2(eltCodecs._1.deserialize(input, protocolVersion), null.asInstanceOf[T2])
    case 1 => target.copy(_2 = eltCodecs._2.deserialize(input, protocolVersion))
  }

  override protected def formatField(source: (T1, T2), index: Int): String = index match {
    case 0 => eltCodecs._1.format(source._1)
    case 1 => eltCodecs._2.format(source._2)
  }

  override protected def parseAndSetField(input: String, target: (T1, T2), index: Int): (T1, T2) = index match {
    case 0 => Tuple2(eltCodecs._1.parse(input), null.asInstanceOf[T2])
    case 1 => target.copy(_2 = eltCodecs._2.parse(input))
  }

}

object Tuple2Codec {

  def apply[T1, T2](eltCodec1: TypeCodec[T1], eltCodec2: TypeCodec[T2]): Tuple2Codec[T1, T2] =
    new Tuple2Codec[T1, T2](eltCodec1, eltCodec2)

  import scala.reflect.runtime.universe._

  def apply[T1, T2](implicit eltTag1: TypeTag[T1], eltTag2: TypeTag[T2]): Tuple2Codec[T1, T2] = {
    val eltCodec1 = TypeConversions.toCodec[T1](eltTag1.tpe)
    val eltCodec2 = TypeConversions.toCodec[T2](eltTag2.tpe)
    apply(eltCodec1, eltCodec2)
  }

}
