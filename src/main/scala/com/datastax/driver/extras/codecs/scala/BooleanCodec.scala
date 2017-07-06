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


object BooleanCodec extends TypeCodec[Boolean](DataType.cboolean(), TypeTokens.boolean)
  with VersionAgnostic[Boolean] {

  private val TRUE = ByteBuffer.wrap(Array[Byte](1))
  private val FALSE = ByteBuffer.wrap(Array[Byte](0))

  override def serialize(value: Boolean, protocolVersion: ProtocolVersion): ByteBuffer =
    if (value) TRUE.duplicate else FALSE.duplicate

  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Boolean = {
    if (bytes == null || bytes.remaining == 0) return false
    if (bytes.remaining != 1) throw new InvalidTypeException("Invalid boolean value, expecting 1 byte but got " + bytes.remaining)
    bytes.get(bytes.position) != 0
  }

  override def format(value: Boolean): String = value.toString

  override def parse(value: String): Boolean = {
    try {
      if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) false
      else value.toBoolean
    }
    catch {
      case e: IllegalArgumentException =>
        throw new InvalidTypeException( s"""Cannot parse boolean value from "$value"""", e)
    }
  }

}
