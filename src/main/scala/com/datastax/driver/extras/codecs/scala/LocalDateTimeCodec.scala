package com.datastax.driver.extras.codecs.scala

import java.nio.ByteBuffer
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.datastax.driver.core.DataType
import com.datastax.driver.core.ProtocolVersion
import com.datastax.driver.core.TypeCodec
import com.google.common.reflect.TypeToken

import scala.util.Success
import scala.util.Try

object LocalDateTimeCodec
  extends TypeCodec[LocalDateTime](
    DataType.timestamp(),
    TypeToken.of(classOf[LocalDateTime]).wrap()
  ) with VersionAgnostic[LocalDateTime] {

  override def serialize(
    localDateTime: LocalDateTime,
    protocolVersion: ProtocolVersion
  ): ByteBuffer =
    Option(localDateTime) match {
      case Some(value) =>
        val milliseconds = value.atZone(ZoneOffset.UTC).toInstant.toEpochMilli
        ByteBuffer.allocate(8).putLong(0, milliseconds)

      // It is used `null` due to serialize requirement
      case _ => null // scalastyle:ignore
    }

  override def deserialize(
    bytes: ByteBuffer,
    protocolVersion: ProtocolVersion
  ): LocalDateTime =
    Option(bytes) match {
      case Some(value) if value.remaining == 8 =>
        val milliseconds = value.getLong(value.position())
        LocalDateTime.ofInstant(
          Instant.ofEpochMilli(milliseconds),
          ZoneOffset.UTC
        )

      case _ => throw new InvalidTypeException(
        s"Invalid 64-bits long value, expecting 8 bytes but got ${bytes.remaining}" // scalastyle:ignore
      )
    }

  override def format(localDateTime: LocalDateTime): String =
    Option(localDateTime) match {
      case Some(value) => value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      case _ => "NULL"
    }

  override def parse(localDateTime: String): LocalDateTime = {

    def parseDateTime: LocalDateTime =
      Try(LocalDateTime.parse(
        localDateTime,
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
      )) match {
        case Success(value) => value
        case _ => throw new IllegalArgumentException(
          s"Illegal datetime format $localDateTime"
        )
      }

    Option(localDateTime) match {
      case Some(value) if value.nonEmpty => parseDateTime

      // It is used `null` due to parse requirement
      case Some(value) if value.isEmpty || value.equalsIgnoreCase("NULL") => null // scalastyle:ignore
      case _ => null // scalastyle:ignore
    }

  }

}