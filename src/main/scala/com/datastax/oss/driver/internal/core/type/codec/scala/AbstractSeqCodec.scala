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
import com.datastax.oss.driver.api.core.`type`.DataType
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.internal.core.`type`.DefaultListType
import com.datastax.oss.driver.internal.core.`type`.codec.ParseUtils

import scala.collection.compat._

abstract class AbstractSeqCodec[T, M[T] <: Seq[T]](
    inner: TypeCodec[T],
    frozen: Boolean
)(implicit factory: Factory[T, M[T]])
    extends TypeCodec[M[T]] {

  override def accepts(value: Any): Boolean = value match {
    case l: M[_] => l.headOption.fold(true)(inner.accepts)
    case _ => false
  }

  override val getCqlType: DataType = new DefaultListType(inner.getCqlType, frozen)

  override def encode(value: M[T], protocolVersion: ProtocolVersion): ByteBuffer =
    if (value == null) null
    else {
      // FIXME this seems pretty costly, we iterate the list several times!
      val buffers = for (item <- value) yield {
        if (item == null) {
          throw new IllegalArgumentException("List elements cannot be null")
        }
        inner.encode(item, protocolVersion)
      }

      pack(buffers.toArray, value.size, protocolVersion)
    }

  override def decode(bytes: ByteBuffer, protocolVersion: ProtocolVersion): M[T] = {
    val builder = factory.newBuilder

    if (bytes == null || bytes.remaining == 0) builder.result()
    else {
      val input = bytes.duplicate()
      val size  = readSize(input, protocolVersion)
      for (_ <- 0 until size) {
        builder += inner.decode(
          readValue(input, protocolVersion),
          protocolVersion
        )
      }

      builder.result()
    }
  }

  override def format(value: M[T]): String =
    if (value == null) {
      "NULL"
    } else {
      value.map(inner.format).mkString("[", ",", "]")
    }

  override def parse(value: String): M[T] =
    if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) {
      null.asInstanceOf[M[T]]
    } else {
      val builder = factory.newBuilder
      var idx     = ParseUtils.skipSpaces(value, 0)
      if (value.charAt(idx) != '[') {
        throw new IllegalArgumentException(
          s"Cannot parse list value from '$value', at character $idx expecting '[' but got '${value.charAt(idx)}''"
        )
      }
      idx = ParseUtils.skipSpaces(value, idx + 1)
      if (value.charAt(idx) == ']') {
        builder.result()
      } else {
        while (idx < value.length) {
          val n = ParseUtils.skipCQLValue(value, idx)
          builder += inner.parse(value.substring(idx, n))

          idx = ParseUtils.skipSpaces(value, n)
          if (idx >= value.length) {
            throw new IllegalArgumentException(
              s"Malformed list value '$value', missing closing ']'"
            )
          } else if (value.charAt(idx) == ']') {
            return builder.result()
          } else if (value.charAt(idx) != ',') {
            throw new IllegalArgumentException(
              s"Cannot parse list value from '$value', at character $idx expecting ',' but got '${value
                .charAt(idx)}''"
            )
          }
          idx = ParseUtils.skipSpaces(value, idx + 1)
        }
        throw new IllegalArgumentException(s"Malformed list value '$value', missing closing ']'")
      }
    }

}
