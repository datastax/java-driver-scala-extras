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
import com.datastax.driver.core.utils.Bytes
import com.datastax.driver.core.{DataType, ProtocolVersion, TypeCodec}
import com.google.common.reflect.TypeToken


object BigIntCodec extends TypeCodec[BigInt](DataType.varint(), TypeToken.of(classOf[BigInt]))
  with VersionAgnostic[BigInt] {

  override def serialize(value: BigInt, protocolVersion: ProtocolVersion): ByteBuffer =
    if (value == null) null else ByteBuffer.wrap(value.toByteArray)

  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): BigInt =
    if (bytes == null || bytes.remaining == 0) null else BigInt(Bytes.getArray(bytes))

  override def format(value: BigInt): String = if (value == null) "NULL" else value.toString

  override def parse(value: String): BigInt = {
    try {
      if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) null
      else BigInt(value)
    }
    catch {
      case e: NumberFormatException =>
        throw new InvalidTypeException( s"""Cannot parse varint value from "$value"""", e)
    }
  }

}
