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

import com.datastax.driver.extras.codecs.scala.Implicits._

class SetCodecSpec extends CodecSpec {

  private val codec = SetCodec[String]

  property("serializing and deserializing a value should result in the same value") {
    forAll { (o: Set[String]) =>
      codec.deserialize(codec.serialize(o)) should equal(o)
    }
  }

  property("formatting and parsing a value should result in the same value") {
    forAll { o: Set[String] =>
      codec.parse(codec.format(o)) should equal(o)
    }
  }

  property("valid strings should be correctly parsed") {
    val validStrings = Table[String, Set[String]](
      ("string", "value"),
      ("{}", Set()),
      ("{''}", Set("")),
      ("{ 'foo', 'bar' }", Set("foo", "bar")),
      ("", Set()), // per convention, collection codecs never return null
      ("NULL", Set())
    )
    forAll(validStrings) { (s: String, o: Set[String]) =>
      codec.parse(s) should equal(o)
    }
  }

  property("valid values should be correctly formatted") {
    val validValues = Table[Set[String], String](
      ("value", "string"),
      (Set(), "{}"),
      (Set(""), "{''}"),
      (Set("foo", "bar"), "{'foo','bar'}"),
      (null, "NULL")
    )
    forAll(validValues) { (o: Set[String], s: String) =>
      codec.format(o) should equal(s)
    }
  }

  property("invalid byte buffers should be rejected when deserializing") {
    forAll(invalidBytes) { (b: ByteBuffer) =>
      an[Exception] should be thrownBy codec.deserialize(b)
    }
  }

  property("invalid strings should be rejected when parsing") {
    val invalidStrings =
      Table(
        "string",
        "Not a valid input",
        "{",
        "{'foo'",
        "}"
      )
    forAll(invalidStrings) { (s: String) =>
      an[Exception] should be thrownBy codec.parse(s)
    }
  }

  property("invalid values should be rejected when serializing") {
    an[NullPointerException] should be thrownBy codec.serialize(Set(null))
  }

}

