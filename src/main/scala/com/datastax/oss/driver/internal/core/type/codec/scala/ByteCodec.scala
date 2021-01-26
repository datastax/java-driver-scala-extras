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

object ByteCodec extends TypeCodec[Byte] {

  def encode(value: Byte, protocolVersion: ProtocolVersion): ByteBuffer =
    ByteBuffer.allocate(1).put(0, value)

  def decode(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Byte =
    if (bytes == null || bytes.remaining == 0) 0
    else if (bytes.remaining != 1)
      throw new IllegalArgumentException(
        s"Invalid byte value, expecting 1 byte but got [${bytes.remaining}]"
      )
    else bytes.get(bytes.position)

  val getCqlType: DataType = DataTypes.TINYINT

  val getJavaType: GenericType[Byte] = GenericType.of(classOf[Byte])

  def format(value: Byte): String =
    value.toString

  def parse(value: String): Byte =
    try {
      if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) 0
      else value.toByte
    } catch {
      case e: NumberFormatException =>
        throw new IllegalArgumentException(s"Cannot parse byte value from [$value]", e)
    }

  override def accepts(javaClass: Class[_]): Boolean = javaClass == classOf[Byte]

  override def accepts(javaType: GenericType[_]): Boolean = javaType == getJavaType

  override def accepts(value: Any): Boolean = value.isInstanceOf[Byte]
}
