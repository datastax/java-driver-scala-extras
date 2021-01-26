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

import java.nio.ByteBuffer

import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import com.datastax.oss.driver.api.core.`type`.{ DataType, DataTypes }

object BooleanCodec extends TypeCodec[Boolean] {

  private val TRUE  = ByteBuffer.wrap(Array[Byte](1))
  private val FALSE = ByteBuffer.wrap(Array[Byte](0))

  def encode(value: Boolean, protocolVersion: ProtocolVersion): ByteBuffer =
    if (value) TRUE.duplicate() else FALSE.duplicate()

  def decode(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Boolean =
    if (bytes == null || bytes.remaining == 0) false
    else if (bytes.remaining != 1)
      throw new IllegalArgumentException(
        s"Invalid boolean value, expecting 1 byte but got [${bytes.remaining}]"
      )
    else bytes.get(bytes.position()) != 0

  val getCqlType: DataType = DataTypes.BOOLEAN

  val getJavaType: GenericType[Boolean] = GenericType.of(classOf[Boolean])

  def format(value: Boolean): String =
    value.toString

  def parse(value: String): Boolean =
    try {
      if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) false
      else value.toBoolean
    } catch {
      case e: IllegalArgumentException =>
        throw new IllegalArgumentException(s"Cannot parse boolean value from [$value]", e)
    }

  override def accepts(javaClass: Class[_]): Boolean = javaClass == classOf[Boolean]

  override def accepts(javaType: GenericType[_]): Boolean = javaType == getJavaType

  override def accepts(value: Any): Boolean = value.isInstanceOf[Boolean]

}
