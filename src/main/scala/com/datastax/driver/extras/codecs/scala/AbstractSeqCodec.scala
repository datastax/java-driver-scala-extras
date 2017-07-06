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

import com.datastax.driver.core.CodecUtils.{readSize, readValue}
import com.datastax.driver.core.DataType.CollectionType
import com.datastax.driver.core._
import com.datastax.driver.core.exceptions.InvalidTypeException
import com.google.common.reflect.TypeToken

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

abstract class AbstractSeqCodec[E, C <: scala.collection.Seq[E]](
    cqlType: CollectionType,
    javaType: TypeToken[C],
    eltCodec: TypeCodec[E])
    (implicit bf: CanBuildFrom[_, E, C])
  extends TypeCodec[C](cqlType, javaType)
    with VersionAgnostic[C] {

  override def serialize(value: C, protocolVersion: ProtocolVersion): ByteBuffer = {
    if (value == null) return null
    val bbs: scala.Seq[ByteBuffer] = for (elt <- value) yield {
      if (elt == null) throw new NullPointerException("List elements cannot be null")
      eltCodec.serialize(elt, protocolVersion)
    }
    CodecUtils.pack(bbs.toArray, value.size, protocolVersion)
  }

  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): C = {
    val builder: mutable.Builder[E, C] = bf()
    if (bytes != null && bytes.remaining > 0) {
      val input: ByteBuffer = bytes.duplicate
      val size: Int = readSize(input, protocolVersion)
      for (_ <- 1 to size)
        builder += eltCodec.deserialize(readValue(input, protocolVersion), protocolVersion)
    }
    builder.result
  }

  override def format(value: C): String =
    if (value == null) "NULL" else '[' + value.map(e => eltCodec.format(e)).mkString(",") + ']'

  override def parse(value: String): C = {
    val builder: mutable.Builder[E, C] = bf()
    if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) return builder.result
    var idx: Int = ParseUtils.skipSpaces(value, 0)
    if (value.charAt(idx) != '[') throw new InvalidTypeException( s"""Cannot parse list value from "$value", at character $idx expecting '[' but got '${value.charAt(idx)}'""")
    idx = ParseUtils.skipSpaces(value, idx + 1)
    if (value.charAt(idx) == ']') return builder.result
    while (idx < value.length) {
      val n = ParseUtils.skipCQLValue(value, idx)
      builder += eltCodec.parse(value.substring(idx, n))
      idx = n
      idx = ParseUtils.skipSpaces(value, idx)
      if (value.charAt(idx) == ']') return builder.result
      if (value.charAt(idx) != ',') throw new InvalidTypeException( s"""Cannot parse list value from "$value", at character $idx expecting ',' but got '${value.charAt(idx)}'""")
      idx = ParseUtils.skipSpaces(value, idx + 1)
    }
    throw new InvalidTypeException( s"""Malformed list value "$value", missing closing ']'""")
  }

  override def accepts(value: AnyRef): Boolean = value match {
    case seq: scala.collection.Seq[_] => if (seq.isEmpty) true else eltCodec.accepts(seq.head)
    case _ => false
  }

}
