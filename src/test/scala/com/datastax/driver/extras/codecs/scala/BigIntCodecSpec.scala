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

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.datastax.driver.extras.codecs.scala.Implicits._

class BigIntCodecSpec extends CodecSpec {

  val codec = BigIntCodec

  property("serializing and deserializing a value should result in the same value") {
    forAll { (o: BigInt) =>
      codec.deserialize(codec.serialize(o)) should equal(o)
    }
  }

  property("formatting and parsing a value should result in the same value") {
    forAll { o: BigInt =>
      val str = codec.format(o)
      codec.parse(str) should equal(o)
    }
  }

  property("valid strings should be correctly parsed") {
    val validStrings = Table(
      ("string", "value"),
      ("0", BigInt(0)),
      ("1", BigInt(1)),
      ("-1234567890", BigInt("-1234567890")),
      ("", null),
      ("NULL", null)
    )
    forAll(validStrings) { (s: String, o: BigInt) =>
      codec.parse(s) should equal(o)
    }
  }

  property("valid values should be correctly formatted") {
    val validValues = Table(
      ("value", "string"),
      (BigInt(0), "0"),
      (BigInt(1), "1"),
      (BigInt("-1234567890"), "-1234567890"),
      (null, "NULL")
    )
    forAll(validValues) { (o: BigInt, s: String) =>
      codec.format(o) should equal(s)
    }
  }

  ignore("invalid byte buffers should be rejected when deserializing") {
    // no buffer is invalid
  }

  property("invalid strings should be rejected when parsing") {
    forAll(invalidStrings) { (s: String) =>
      an[InvalidTypeException] should be thrownBy codec.parse(s)
    }
  }

}

