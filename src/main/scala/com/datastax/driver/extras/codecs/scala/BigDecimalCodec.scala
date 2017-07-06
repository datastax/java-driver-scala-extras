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
import com.google.common.reflect.TypeToken

object BigDecimalCodec extends TypeCodec[BigDecimal](DataType.decimal(), TypeToken.of(classOf[BigDecimal]))
  with VersionAgnostic[BigDecimal] {

  override def serialize(value: BigDecimal, protocolVersion: ProtocolVersion): ByteBuffer = {
    if (value == null) return null
    val bi = value.bigDecimal.unscaledValue
    val scale = value.scale
    val bibytes = bi.toByteArray
    val bytes = ByteBuffer.allocate(4 + bibytes.length)
    bytes.putInt(scale)
    bytes.put(bibytes)
    bytes.rewind
    bytes
  }

  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): BigDecimal = {
    if (bytes == null || bytes.remaining == 0) return null
    if (bytes.remaining < 4) throw new InvalidTypeException("Invalid decimal value, expecting at least 4 bytes but got " + bytes.remaining)
    val bb = bytes.duplicate
    val scale = bb.getInt
    val bibytes = new Array[Byte](bb.remaining)
    bb.get(bibytes)
    BigDecimal(BigInt(bibytes), scale)
  }

  override def format(value: BigDecimal): String = if (value == null) "NULL" else value.toString

  override def parse(value: String): BigDecimal = {
    try {
      if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) null
      else BigDecimal(value)
    }
    catch {
      case e: NumberFormatException =>
        throw new InvalidTypeException( s"""Cannot parse varint value from "$value"""", e)
    }
  }

}
