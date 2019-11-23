package com.datastax.driver.extras.codecs.scala

import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.datastax.driver.core.CodecUtils
import com.datastax.driver.core.DataType
import com.datastax.driver.core.ProtocolVersion
import com.datastax.driver.core.TypeCodec
import com.google.common.reflect.TypeToken

import scala.util.Success
import scala.util.Try

object LocalDateCodec
  extends TypeCodec[LocalDate](
    DataType.date(),
    TypeToken.of(classOf[LocalDate]).wrap()
  ) with VersionAgnostic[LocalDate] {

  override def serialize(
    localDate: LocalDate,
    protocolVersion: ProtocolVersion
  ): ByteBuffer =
    Option(localDate) match {
      case Some(value) =>
        val epoch = LocalDate.ofEpochDay(0)
        val daysSinceEpoch = ChronoUnit.DAYS.between(epoch, value)
        val unsigned = CodecUtils.fromSignedToUnsignedInt(daysSinceEpoch.toInt)
        ByteBuffer.allocate(4).putInt(0, unsigned)

      // It is used `null` due to serialize requirement
      case _ => null // scalastyle:ignore
    }

  override def deserialize(
    bytes: ByteBuffer,
    protocolVersion: ProtocolVersion
  ): LocalDate =
    Option(bytes) match {
      case Some(value) if value.remaining == 4 =>
        val unsigned = value.getInt(value.position)
        val daysSinceEpoch = CodecUtils.fromUnsignedToSignedInt(unsigned)
        LocalDate.ofEpochDay(daysSinceEpoch)

      case Some(value) if value.remaining == 0 || value.remaining != 4 =>
        throw new InvalidTypeException(
          s"Invalid 32-bits integer value, expecting 4 bytes but got ${bytes.remaining}" // scalastyle:ignore
        )

      // It is used `null` due to deserialize requirement
      case _ => null // scalastyle:ignore
    }

  override def format(localDate: LocalDate): String =
    Option(localDate) match {
      case Some(value) => value.format(DateTimeFormatter.ISO_LOCAL_DATE)
      case _ => "NULL"
    }

  override def parse(localDate: String): LocalDate = {

    def parseDate: LocalDate =
      Try(LocalDate.parse(localDate, DateTimeFormatter.ISO_LOCAL_DATE)) match {
        case Success(value) => value
        case _ => throw new IllegalArgumentException(
          s"Illegal date format $localDate"
        )
      }

    Option(localDate) match {
      case Some(value) if value.nonEmpty => parseDate

      // It is used `null` due to parse requirement
      case Some(value) if value.isEmpty || value.equalsIgnoreCase("NULL") => null // scalastyle:ignore
      case _ => null // scalastyle:ignore
    }

  }

}
