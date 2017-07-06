package com.datastax.driver.extras.codecs.scala

import java.nio.ByteBuffer

import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, PropSpec}

/**
  *
  */
abstract class CodecSpec extends PropSpec with PropertyChecks with Matchers {

  val invalidBytes =
    Table(
      "buffer",
      ByteBuffer.allocate(1)
    )

  val invalidStrings =
    Table(
      "string",
      "Not a valid input"
    )

}
