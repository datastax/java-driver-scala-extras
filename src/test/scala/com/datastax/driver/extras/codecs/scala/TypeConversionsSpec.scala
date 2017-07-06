/*
 * Copyright 2017 DataStax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datastax.driver.extras.codecs.scala

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import com.datastax.driver.core.{Duration, TypeCodec}
import com.datastax.driver.extras.codecs.jdk8.{InstantCodec, LocalDateCodec, LocalTimeCodec}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, PropSpec}

import scala.reflect.runtime.universe._

/**
  *
  */
class TypeConversionsSpec extends PropSpec with PropertyChecks with Matchers {

  property("A known type should be converted to the appropriate codec") {
    val validTypes =
      Table[Type, TypeCodec[_]](

        ("type", "codec"),

        (typeOf[Boolean], BooleanCodec),
        (typeOf[Byte], ByteCodec),
        (typeOf[Short], ShortCodec),
        (typeOf[Int], IntCodec),
        (typeOf[Long], LongCodec),
        (typeOf[Float], FloatCodec),
        (typeOf[Double], DoubleCodec),

        (typeOf[String], TypeCodec.varchar()),
        (typeOf[BigInt], BigIntCodec),
        (typeOf[BigDecimal], BigDecimalCodec),

        (typeOf[ByteBuffer], TypeCodec.blob()),
        (typeOf[Date], TypeCodec.timestamp()),
        (typeOf[Duration], TypeCodec.duration()),
        (typeOf[com.datastax.driver.core.LocalDate], TypeCodec.date()),
        (typeOf[InetAddress], TypeCodec.inet()),
        (typeOf[UUID], TypeCodec.uuid()),

        (typeOf[java.time.Instant], InstantCodec.instance),
        (typeOf[java.time.LocalDate], LocalDateCodec.instance),
        (typeOf[java.time.LocalTime], LocalTimeCodec.instance),

        (typeOf[Option[Boolean]], OptionCodec[Boolean]),
        (typeOf[Option[Byte]], OptionCodec[Byte]),
        (typeOf[Option[Short]], OptionCodec[Short]),
        (typeOf[Option[Int]], OptionCodec[Int]),
        (typeOf[Option[Long]], OptionCodec[Long]),
        (typeOf[Option[Float]], OptionCodec[Float]),
        (typeOf[Option[Double]], OptionCodec[Double]),

        (typeOf[Option[Int]], OptionCodec[Int]),
        (typeOf[Option[String]], OptionCodec[String]),
        (typeOf[Option[Map[Int, Option[Seq[Set[Double]]]]]], OptionCodec[Map[Int, Option[Seq[Set[Double]]]]]),

        (typeOf[Seq[Int]], SeqCodec[Int]),
        (typeOf[Seq[Map[Int, Option[Seq[Set[Double]]]]]], SeqCodec[Map[Int, Option[Seq[Set[Double]]]]]),

        (typeOf[Set[Int]], SetCodec[Int]),
        (typeOf[Set[Map[Int, Option[Seq[Set[Double]]]]]], SetCodec[Map[Int, Option[Seq[Set[Double]]]]]),

        (typeOf[Map[Int, String]], MapCodec[Int, String]),
        (typeOf[Map[Option[Seq[Double]], Map[Int, Option[Seq[Set[Double]]]]]], MapCodec[Option[Seq[Double]], Map[Int, Option[Seq[Set[Double]]]]]),

        (typeOf[Tuple2[Int, String]], Tuple2Codec[Int, String]),
        (typeOf[Tuple2[Option[Seq[Double]], Map[Int, Option[Seq[Set[Double]]]]]], Tuple2Codec[Option[Seq[Double]], Map[Int, Option[Seq[Set[Double]]]]]),

        (typeOf[Tuple3[Int, String, Date]], Tuple3Codec[Int, String, Date]),
        (typeOf[Tuple3[Option[Seq[Double]], Map[Int, Option[Seq[Set[Double]]]], InetAddress]], Tuple3Codec[Option[Seq[Double]], Map[Int, Option[Seq[Set[Double]]]], InetAddress])

      )
    forAll(validTypes) { (tpe: Type, expected: TypeCodec[_]) =>
      val actual = TypeConversions.toCodec(tpe)
      actual.getJavaType should equal(expected.getJavaType)
      actual.getCqlType should equal(expected.getCqlType)
      actual.accepts(expected.getJavaType) shouldBe true
      expected.accepts(actual.getJavaType) shouldBe true
    }
  }

}
