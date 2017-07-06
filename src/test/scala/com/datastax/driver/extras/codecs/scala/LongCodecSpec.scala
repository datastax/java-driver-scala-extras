/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.extras.codecs.scala

import java.nio.ByteBuffer

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.datastax.driver.extras.codecs.scala.Implicits._

class LongCodecSpec extends CodecSpec {

  val codec = LongCodec

  property("serializing and deserializing a value should result in the same value") {
    forAll { (o: Long) =>
      codec.deserialize(codec.serialize(o)) should equal(o)
    }
  }

  property("formatting and parsing a value should result in the same value") {
    forAll("value") { o: Long =>
      codec.parse(codec.format(o)) should equal(o)
    }
  }

  property("valid strings should be correctly parsed") {
    val validStrings = Table[String, Long](
      ("string", "value"),
      ("0", 0),
      ("1", 1),
      ("-9223372036854775808", 0x8000000000000000L),
      ("9223372036854775807", 0x7fffffffffffffffL),
      ("", 0), // Long is not nullable
      ("NULL", 0)
    )
    forAll(validStrings) { (s: String, o: Long) =>
      codec.parse(s) should equal(o)
    }
  }

  property("valid values should be correctly formatted") {
    val validValues = Table[Long, String](
      ("value", "string"),
      (0, "0"),
      (1, "1"),
      (0x8000000000000000L, "-9223372036854775808"),
      (0x7fffffffffffffffL, "9223372036854775807")
    )
    forAll(validValues) { (o: Long, s: String) =>
      codec.format(o) should equal(s)
    }
  }

  property("invalid byte buffers should be rejected when deserializing") {
    forAll(invalidBytes) { (b: ByteBuffer) =>
      an[InvalidTypeException] should be thrownBy codec.deserialize(b)
    }
  }

  property("invalid strings should be rejected when parsing") {
    val invalidStrings =
      Table(
        "string",
        "Not a valid input",
        "9223372036854775808"
      )
    forAll(invalidStrings) { (s: String) =>
      an[InvalidTypeException] should be thrownBy codec.parse(s)
    }
  }

}

