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

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.datastax.driver.core.{DataType, ProtocolVersion, TypeCodec}

object ByteCodec extends TypeCodec[Byte](DataType.tinyint(), TypeTokens.byte)
  with VersionAgnostic[Byte] {

  override def serialize(value: Byte, protocolVersion: ProtocolVersion): ByteBuffer =
    ByteBuffer.allocate(1).put(0, value)

  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Byte = {
    if (bytes == null || bytes.remaining == 0) return 0
    if (bytes.remaining != 1) throw new InvalidTypeException("Invalid byte value, expecting 1 byte but got " + bytes.remaining)
    bytes.get(bytes.position)
  }

  override def format(value: Byte): String = value.toString

  override def parse(value: String): Byte = {
    try {
      if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) 0
      else value.toByte
    }
    catch {
      case e: NumberFormatException =>
        throw new InvalidTypeException( s"""Cannot parse byte value from "$value"""", e)
    }
  }

}
