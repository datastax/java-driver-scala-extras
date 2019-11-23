package com.datastax.driver.extras.codecs.scala

import java.time.LocalDate
import java.time.LocalDateTime

import com.datastax.driver.core._

trait CassandraCodecs {

  val bigDecimalCodec: TypeCodec[BigDecimal] = BigDecimalCodec2
  val localDateCodec: TypeCodec[LocalDate] = LocalDateCodec
  val localDateTimeCodec: TypeCodec[LocalDateTime] = LocalDateTimeCodec

  def registerCodecs(cassandraSession: Session): CodecRegistry = {
    cassandraSession.getCluster.getConfiguration.getCodecRegistry
      .register(bigDecimalCodec, localDateCodec, localDateTimeCodec)
  }

}
