package com.datastax.driver.extras.codecs.scala

import java.nio.ByteBuffer

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.datastax.driver.core.DataType
import com.datastax.driver.core.ProtocolVersion
import com.datastax.driver.core.TypeCodec
import com.google.common.reflect.TypeToken

import scala.util.Success
import scala.util.Try

object BigDecimalCodec2
  extends TypeCodec[BigDecimal](
    DataType.decimal(),
    TypeToken.of(classOf[BigDecimal]).wrap()
  ) with VersionAgnostic[BigDecimal] {

  override def serialize(
    bigDecimal: BigDecimal,
    protocolVersion: ProtocolVersion
  ): ByteBuffer =
    Option(bigDecimal) match {
      case Some(value) =>
        val bigInteger = value.bigDecimal.unscaledValue()
        val scale = value.scale
        val bigIntegerBytes = bigInteger.toByteArray

        val bytes = ByteBuffer.allocate(4 + bigIntegerBytes.length)
        bytes.putInt(scale)
        bytes.put(bigIntegerBytes)
        bytes.rewind
        bytes

      // It is used `null` due to serialize requirement
      case _ => null // scalastyle:ignore
    }

  override def deserialize(
    bytes: ByteBuffer,
    protocolVersion: ProtocolVersion
  ): BigDecimal =
    Option(bytes) match {
      case Some(value) if value.remaining >= 4 =>
        val byteBuffer = bytes.duplicate
        val scale = byteBuffer.getInt
        val byteArray = new Array[Byte](byteBuffer.remaining)
        byteBuffer.get(byteArray)
        BigDecimal(BigInt(byteArray), scale)

      case Some(value) if value.remaining < 4 => throw new InvalidTypeException(
        s"Invalid decimal value, expecting at least 4 bytes but got ${bytes.remaining}" // scalastyle:ignore
      )

      // It is used `null` due to deserialize requirement
      case Some(value) if value.remaining == 0 => null // scalastyle:ignore
      case _ => null // scalastyle:ignore
    }

  override def format(bigDecimal: BigDecimal): String =
    Option(bigDecimal) match {
      case Some(value) => value.toString()
      case _ => "NULL"
    }

  override def parse(bigDecimal: String): BigDecimal = {

    def parseDecimal: BigDecimal =
      Try(BigDecimal(bigDecimal)) match {
        case Success(value) => value
        case _ => throw new IllegalArgumentException(
          s"Cannot parse decimal value from $bigDecimal"
        )
      }

    Option(bigDecimal) match {
      // It is used `null` due to parse requirement
      case Some(value)
        if value.isEmpty || value.equalsIgnoreCase("NULL") || value == null => null // scalastyle:ignore
      case Some(value) if value.nonEmpty => parseDecimal
    }
  }

}
