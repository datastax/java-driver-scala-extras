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
import com.datastax.oss.driver.api.core.`type`.reflect.{ GenericType, GenericTypeParameter }
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken

class OptionCodec[T](inner: TypeCodec[T]) extends TypeCodec[Option[T]] {

  override val getJavaType: GenericType[Option[T]] = {
    GenericType
      .of(new TypeToken[Option[T]]() {}.getType)
      .where(new GenericTypeParameter[T] {}, inner.getJavaType.wrap())
      .asInstanceOf[GenericType[Option[T]]]
  }

  override val getCqlType: DataType = inner.getCqlType

  override def encode(value: Option[T], protocolVersion: ProtocolVersion): ByteBuffer =
    value match {
      case Some(value) => inner.encode(value, protocolVersion)
      case None =>
        null // FIXME this would create a tombstone, although this is how `OptionalCodec` does it. A higher level solution is needed (eg. PSTMT unset)
    }

  override def decode(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Option[T] =
    if (bytes == null || bytes.remaining == 0) None
    else Option(inner.decode(bytes, protocolVersion))

  override def format(value: Option[T]): String = value match {
    case Some(value) => inner.format(value)
    case None => "NULL"
  }

  override def parse(value: String): Option[T] =
    if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) None
    else Option(inner.parse(value))

  override def accepts(value: Any): Boolean = value match {
    case None => true
    case Some(value) => inner.accepts(value)
    case _ => false
  }

}

object OptionCodec {
  def apply[T](inner: TypeCodec[T]): OptionCodec[T] = new OptionCodec(inner)
}
