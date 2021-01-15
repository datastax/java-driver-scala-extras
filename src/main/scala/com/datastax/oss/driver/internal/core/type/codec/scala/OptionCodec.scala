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
