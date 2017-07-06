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


object LongCodec extends TypeCodec[Long](DataType.bigint(), TypeTokens.long)
  with VersionAgnostic[Long] {

  override def serialize(value: Long, protocolVersion: ProtocolVersion): ByteBuffer =
    ByteBuffer.allocate(8).putLong(0, value)

  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Long = {
    if (bytes == null || bytes.remaining == 0) return 0
    if (bytes.remaining != 8) throw new InvalidTypeException("Invalid 64-bits long value, expecting 8 bytes but got " + bytes.remaining)
    bytes.getLong(bytes.position)
  }

  override def format(value: Long): String = value.toString

  override def parse(value: String): Long = {
    try {
      if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) 0
      else value.toLong
    }
    catch {
      case e: NumberFormatException =>
        throw new InvalidTypeException( s"""Cannot parse 64-bits long value from "$value"""", e)
    }
  }

}
