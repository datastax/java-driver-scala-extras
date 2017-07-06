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

class IntCodecSpec extends CodecSpec {

  val codec = IntCodec

  property("serializing and deserializing a value should result in the same value") {
    forAll { (o: Int) =>
      codec.deserialize(codec.serialize(o)) should equal(o)
    }
  }

  property("formatting and parsing a value should result in the same value") {
    forAll("value") { o: Int =>
      codec.parse(codec.format(o)) should equal(o)
    }
  }

  property("valid strings should be correctly parsed") {
    val validStrings = Table[String, Int](
      ("string", "value"),
      ("0", 0),
      ("1", 1),
      ("-2147483648", 0x80000000),
      ("2147483647", 0x7fffffff),
      ("", 0), // Int is not nullable
      ("NULL", 0)
    )
    forAll(validStrings) { (s: String, o: Int) =>
      codec.parse(s) should equal(o)
    }
  }

  property("valid values should be correctly formatted") {
    val validValues = Table[Int, String](
      ("value", "string"),
      (0, "0"),
      (1, "1"),
      (0x80000000, "-2147483648"),
      (0x7fffffff, "2147483647")
    )
    forAll(validValues) { (o: Int, s: String) =>
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
        "2147483648"
      )
    forAll(invalidStrings) { (s: String) =>
      an[InvalidTypeException] should be thrownBy codec.parse(s)
    }
  }

}

