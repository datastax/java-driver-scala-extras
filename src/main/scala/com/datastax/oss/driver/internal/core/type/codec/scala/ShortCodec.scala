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

object ShortCodec extends TypeCodec[Short] {

  def encode(value: Short, protocolVersion: ProtocolVersion): ByteBuffer =
    ByteBuffer.allocate(2).putShort(0, value)

  def decode(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Short =
    if (bytes == null || bytes.remaining == 0) 0
    else if (bytes.remaining != 2)
      throw new IllegalArgumentException(
        s"Invalid 16-bits integer value, expecting 2 bytes but got [${bytes.remaining}]"
      )
    else bytes.getShort(bytes.position)

  val getCqlType: DataType = DataTypes.SMALLINT

  val getJavaType: GenericType[Short] = GenericType.of(classOf[Short])

  def format(value: Short): String =
    value.toString

  def parse(value: String): Short =
    try {
      if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) 0
      else value.toShort
    } catch {
      case e: NumberFormatException =>
        throw new IllegalArgumentException(s"Cannot parse 16-bits int value from [$value]", e)
    }

  override def accepts(javaClass: Class[_]): Boolean = javaClass == classOf[Short]

  override def accepts(javaType: GenericType[_]): Boolean = javaType == getJavaType

  override def accepts(value: Any): Boolean = value.isInstanceOf[Short]
}
