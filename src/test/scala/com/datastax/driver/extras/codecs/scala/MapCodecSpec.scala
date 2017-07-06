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

class MapCodecSpec extends CodecSpec {

  private val codec = MapCodec[String, String]

  property("serializing and deserializing a value should result in the same value") {
    forAll { (o: Map[String, String]) =>
      codec.deserialize(codec.serialize(o)) should equal(o)
    }
  }

  property("formatting and parsing a value should result in the same value") {
    forAll { o: Map[String, String] =>
      codec.parse(codec.format(o)) should equal(o)
    }
  }

  property("valid strings should be correctly parsed") {
    val validStrings = Table[String, Map[String, String]](
      ("string", "value"),
      ("{}", Map()),
      ("{'foo':''}", Map("foo" -> "")),
      ("{ 'key1' : 'value1', 'key2' : 'value2' }", Map("key1" -> "value1", "key2" -> "value2")),
      ("", Map()), // per convention, collection codecs never return null
      ("NULL", Map())
    )
    forAll(validStrings) { (s: String, o: Map[String, String]) =>
      codec.parse(s) should equal(o)
    }
  }

  property("valid values should be correctly formatted") {
    val validValues = Table[Map[String, String], String](
      ("value", "string"),
      (Map(), "{}"),
      (Map("foo" -> ""), "{'foo':''}"),
      (Map("key1" -> "value1", "key2" -> "value2"), "{'key1':'value1','key2':'value2'}"),
      (null, "NULL")
    )
    forAll(validValues) { (o: Map[String, String], s: String) =>
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
        "{1",
        "{1:",
        "{:",
        "}"
      )
    forAll(invalidStrings) { (s: String) =>
      an[Exception] should be thrownBy codec.parse(s)
    }
  }

  property("invalid values should be rejected when serializing") {
    val invalidValues = Table[Map[String, String]](
      "value",
      Map(null.asInstanceOf[String] -> "foo"),
      Map("foo" -> null),
      Map(null.asInstanceOf[String] -> null)
    )
    forAll(invalidValues) { (o: Map[String, String]) =>
      an[Exception] should be thrownBy codec.serialize(o)
    }
  }

}

