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


object ShortCodec extends TypeCodec[Short](DataType.smallint(), TypeTokens.short)
  with VersionAgnostic[Short] {

  override def serialize(value: Short, protocolVersion: ProtocolVersion): ByteBuffer =
    ByteBuffer.allocate(2).putShort(0, value)

  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Short = {
    if (bytes == null || bytes.remaining == 0) return 0
    if (bytes.remaining != 2) throw new InvalidTypeException("Invalid 16-bits short value, expecting 2 bytes but got " + bytes.remaining)
    bytes.getShort(bytes.position)
  }

  override def format(value: Short): String = value.toString

  override def parse(value: String): Short = {
    try {
      if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) 0
      else value.toShort
    }
    catch {
      case e: NumberFormatException =>
        throw new InvalidTypeException( s"""Cannot parse 16-bits short value from "$value"""", e)
    }
  }

}
