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

class ByteCodecSpec extends CodecSpec {

  val codec = ByteCodec

  property("serializing and deserializing a value should result in the same value") {
    forAll { (o: Byte) =>
      codec.deserialize(codec.serialize(o)) should equal(o)
    }
  }

  property("formatting and parsing a value should result in the same value") {
    forAll("value") { o: Byte =>
      codec.parse(codec.format(o)) should equal(o)
    }
  }

  property("valid strings should be correctly parsed") {
    val validStrings = Table[String, Byte](
      ("string", "value"),
      ("0", 0),
      ("1", 1),
      ("-128", -128),
      ("127", 127),
      ("", 0), // Byte is not nullable
      ("NULL", 0)
    )
    forAll(validStrings) { (s: String, o: Byte) =>
      codec.parse(s) should equal(o)
    }
  }

  property("valid values should be correctly formatted") {
    val validValues = Table[Byte, String](
      ("value", "string"),
      (0, "0"),
      (1, "1"),
      (-128, "-128"),
      (127, "127")
    )
    forAll(validValues) { (o: Byte, s: String) =>
      codec.format(o) should equal(s)
    }
  }

  property("invalid byte buffers should be rejected when deserializing") {
    val invalidBytes =
      Table(
        "buffer",
        ByteBuffer.allocate(2)
      )
    forAll(invalidBytes) { (b: ByteBuffer) =>
      an[InvalidTypeException] should be thrownBy codec.deserialize(b)
    }
  }

  property("invalid strings should be rejected when parsing") {
    val invalidStrings =
      Table(
        "string",
        "Not a valid input",
        "128"
      )
    forAll(invalidStrings) { (s: String) =>
      an[InvalidTypeException] should be thrownBy codec.parse(s)
    }
  }

}

