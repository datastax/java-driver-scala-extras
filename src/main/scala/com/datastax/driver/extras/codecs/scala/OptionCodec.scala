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

import com.datastax.driver.core.{DataType, ProtocolVersion, TypeCodec}
import com.google.common.reflect.TypeToken

class OptionCodec[T](
    cqlType: DataType,
    javaType: TypeToken[Option[T]],
    innerCodec: TypeCodec[T])
  extends TypeCodec[Option[T]](cqlType, javaType)
    with VersionAgnostic[Option[T]] {

  def this(innerCodec: TypeCodec[T]) {
    this(innerCodec.getCqlType, TypeTokens.optionOf(innerCodec.getJavaType), innerCodec)
  }

  override def serialize(value: Option[T], protocolVersion: ProtocolVersion): ByteBuffer =
    if (value.isEmpty) OptionCodec.empty.duplicate else innerCodec.serialize(value.get, protocolVersion)

  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Option[T] =
    if (bytes == null || bytes.remaining() == 0) None else Option(innerCodec.deserialize(bytes, protocolVersion))

  override def format(value: Option[T]): String =
    if (value.isEmpty) "NULL" else innerCodec.format(value.get)

  override def parse(value: String): Option[T] =
    if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) None else Option(innerCodec.parse(value))

}

object OptionCodec {

  private val empty = ByteBuffer.allocate(0)

  def apply[T](innerCodec: TypeCodec[T]): OptionCodec[T] =
    new OptionCodec[T](innerCodec)

  import scala.reflect.runtime.universe._

  def apply[T](implicit innerTag: TypeTag[T]): OptionCodec[T] = {
    val innerCodec = TypeConversions.toCodec[T](innerTag.tpe)
    apply(innerCodec)
  }

}
