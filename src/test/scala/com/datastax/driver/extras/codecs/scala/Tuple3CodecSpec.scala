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

import com.datastax.driver.core.DataType.{cboolean, cint}
import com.datastax.driver.core._
import com.datastax.driver.core.exceptions.InvalidTypeException
import com.datastax.driver.extras.codecs.scala.Implicits._
import com.google.common.reflect.TypeToken

class Tuple3CodecSpec extends CodecSpec {

  private val codec = Tuple3Codec[Int, Int, Int]

  property("serializing and deserializing a value should result in the same value") {
    forAll { (o: Tuple3[Int, Int, Int]) =>
      codec.deserialize(codec.serialize(o)) should equal(o)
    }
  }

  property("formatting and parsing a value should result in the same value") {
    forAll { o: (Int, Int, Int) =>
      codec.parse(codec.format(o)) should equal(o)
    }
  }

  property("valid strings should be correctly parsed") {
    val validStrings = Table[String, (Int, Int, Int)](
      ("string", "value"),
      ("(0,0,0)", Tuple3(0, 0, 0)),
      (" ( 1 , 1 , 1 ) ", Tuple3(1, 1, 1)),
      ("", null),
      ("NULL", null)
    )
    forAll(validStrings) { (s: String, o: (Int, Int, Int)) =>
      codec.parse(s) should equal(o)
    }
  }

  property("valid values should be correctly formatted") {
    val validValues = Table[(Int, Int, Int), String](
      ("value", "string"),
      (Tuple3(0, 0, 0), "(0,0,0)"),
      (Tuple3(1, 1, 1), "(1,1,1)"),
      (null, "NULL")
    )
    forAll(validValues) { (o: (Int, Int, Int), s: String) =>
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
        "(",
        "(1",
        "(1,",
        "(,",
        ")"
      )
    forAll(invalidStrings) { (s: String) =>
      an[Exception] should be thrownBy codec.parse(s)
    }
  }

  property("invalid tuple types should be rejected when instantiating codec") {
    val invalidCqlTypes = Table(
      "type",
      Tuples.of(cint()),
      Tuples.of(cboolean(), cint(), cint()),
      Tuples.of(cint(), cboolean(), cint()),
      Tuples.of(cint(), cint(), cboolean())
    )
    val javaType = TypeTokens.tuple3Of(TypeTokens.int, TypeTokens.int, TypeTokens.int)
    forAll(invalidCqlTypes) { (cqlType: TupleType) =>
      an[IllegalArgumentException] should be thrownBy new Tuple3Codec[Int, Int, Int](cqlType, javaType, (IntCodec, IntCodec, IntCodec))
    }
  }

}

