package com.datastax.oss.driver.internal.core.`type`.codec.scala

import java.nio.ByteBuffer

import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import com.datastax.oss.driver.api.core.`type`.{DataType, DataTypes}

object LongCodec extends TypeCodec[Long] {

  def encode(value: Long, protocolVersion: ProtocolVersion): ByteBuffer =
    ByteBuffer.allocate(8).putLong(0, value)

  def decode(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Long =
    if (bytes == null || bytes.remaining == 0) 0
    else if (bytes.remaining != 8)
      throw new IllegalArgumentException(
        s"Invalid 64-bits integer value, expecting 8 bytes but got [${bytes.remaining}]"
      )
    else bytes.getLong(bytes.position)

  def getCqlType(): DataType =
    DataTypes.DOUBLE

  def getJavaType(): GenericType[Long] =
    GenericType.of(classOf[Long])

  def format(value: Long): String =
    value.toString

  def parse(value: String): Long =
    try {
      if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) 0
      else value.toLong
    } catch {
      case e: NumberFormatException =>
        throw new IllegalArgumentException(s"Cannot parse 64-bits integer value from [$value]", e)
    }

  override def accepts(javaClass: Class[_]): Boolean = javaClass == classOf[Long]

  override def accepts(javaType: GenericType[_]): Boolean = javaType == getJavaType()

  override def accepts(value: Any): Boolean = value.isInstanceOf[Long]
}