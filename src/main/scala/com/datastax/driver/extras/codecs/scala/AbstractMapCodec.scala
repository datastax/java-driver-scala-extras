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

import com.datastax.driver.core.DataType.CollectionType
import com.datastax.driver.core._
import com.datastax.driver.core.exceptions.InvalidTypeException
import com.google.common.reflect.TypeToken

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class AbstractMapCodec[K, V, C <: scala.collection.Map[K, V]](
    cqlType: CollectionType,
    javaType: TypeToken[C],
    keyCodec: TypeCodec[K],
    valueCodec: TypeCodec[V])
    (implicit bf: CanBuildFrom[_, (K, V), C])
  extends TypeCodec[C](cqlType, javaType)
    with VersionAgnostic[C] {

  override def serialize(value: C, protocolVersion: ProtocolVersion): ByteBuffer = {
    if (value == null) return null
    val bbs = new ArrayBuffer[ByteBuffer](2 * value.size)
    for ((k, v) <- value) {
      if (k == null) throw new NullPointerException("Map keys cannot be null")
      if (v == null) throw new NullPointerException("Map values cannot be null")
      bbs += keyCodec.serialize(k, protocolVersion)
      bbs += valueCodec.serialize(v, protocolVersion)
    }
    CodecUtils.pack(bbs.toArray, value.size, protocolVersion)
  }

  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): C = {
    val builder: mutable.Builder[(K, V), C] = bf()
    if (bytes != null && bytes.remaining > 0) {
      val input = bytes.duplicate
      val n = CodecUtils.readSize(input, protocolVersion)
      for (_ <- 1 to n) {
        val k = keyCodec.deserialize(CodecUtils.readValue(input, protocolVersion), protocolVersion)
        val v = valueCodec.deserialize(CodecUtils.readValue(input, protocolVersion), protocolVersion)
        builder += ((k, v))
      }
    }
    builder.result
  }

  override def format(value: C): String =
    if (value == null) "NULL" else '{' + value.map { case (k, v) => s"${keyCodec.format(k)}:${valueCodec.format(v)}" }.mkString(",") + '}'

  override def parse(value: String): C = {
    val builder: mutable.Builder[(K, V), C] = bf()
    if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) return builder.result
    var idx = ParseUtils.skipSpaces(value, 0)
    if (value.charAt(idx) != '{') throw new InvalidTypeException( s"""Cannot parse map value from "$value", at character $idx expecting '{' but got '${value.charAt(idx)}'""")
    idx = ParseUtils.skipSpaces(value, idx + 1)
    if (value.charAt(idx) == '}') return builder.result
    while (idx < value.length) {
      var n = ParseUtils.skipCQLValue(value, idx)
      val k = keyCodec.parse(value.substring(idx, n))
      idx = n
      idx = ParseUtils.skipSpaces(value, idx)
      if (value.charAt(idx) != ':') throw new InvalidTypeException( s"""Cannot parse map value from "$value", at character $idx expecting ':' but got '${value.charAt(idx)}'""")
      idx = ParseUtils.skipSpaces(value, idx + 1)
      n = ParseUtils.skipCQLValue(value, idx)
      val v = valueCodec.parse(value.substring(idx, n))
      builder += ((k, v))
      idx = n
      idx = ParseUtils.skipSpaces(value, idx)
      if (value.charAt(idx) == '}') return builder.result
      if (value.charAt(idx) != ',') throw new InvalidTypeException( s"""Cannot parse map value from "$value", at character $idx expecting ',' but got '${value.charAt(idx)}'""")
      idx = ParseUtils.skipSpaces(value, idx + 1)
    }
    throw new InvalidTypeException( s"""Malformed map value "$value", missing closing '}'""")
  }

  override def accepts(value: AnyRef): Boolean = value match {
    case map: scala.collection.Map[_, _] => if (map.isEmpty) true else keyCodec.accepts(map.head._1) && valueCodec.accepts(map.head._2)
    case _ => false
  }

}
