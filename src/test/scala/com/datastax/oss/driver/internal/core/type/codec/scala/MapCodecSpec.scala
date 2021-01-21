package com.datastax.oss.driver.internal.core.`type`.codec.scala

import java.util

import com.datastax.oss.driver.api.core.`type`.codec.{ TypeCodec, TypeCodecs }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MapCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[Map[String, Int]] {

  override protected val codec: TypeCodec[Map[String, Int]] =
    MapCodec.frozen(TypeCodecs.TEXT, IntCodec)

  "MapCodec" should {
    val value = Map("Foo" -> 1, "Bar" -> 2, "Baz" -> 3)
    "encode" in {
      encode(null) shouldBe None
      encode(Map.empty[String, Int]) shouldBe Some("0x00000000")
      encode(value) shouldBe Some(
        "0x0000000300000003466f6f00000004000000010000000342617200000004000000020000000342617a0000000400000003"
      )
    }

    "decode" in {
      decode(null) shouldBe Some(Map.empty[String, Int])
      decode("0x00000000") shouldBe Some(Map.empty[String, Int])
      decode(
        "0x0000000300000003466f6f00000004000000010000000342617200000004000000020000000342617a0000000400000003"
      ) shouldBe Some(
        value
      )
    }

    "format" in {
      format(Map.empty[String, Int]) shouldBe "{}"
      format(value) shouldBe "{'Foo':1,'Bar':2,'Baz':3}"
    }

    "parse" in {
      parse("") shouldBe null
      parse("NULL") shouldBe null
      parse("{}") shouldBe Map.empty[String, Int]
      parse("{'Foo':1,'Bar':2,'Baz':3}") shouldBe value
      parse(" { 'Foo' : 1 , 'Bar' : 2 , 'Baz' : 3 } ") shouldBe value
    }

    "fail to parse invalid input" in {
      val invalid = Seq(
        "Foo:1,Bar:2,Baz:3}",
        "{Foo:1,Bar:2,Baz:3",
        "{{Foo:1,Bar:2,Baz:3}",
        "{Foo,Bar:2,Baz:3}",
        "{Foo:1 Bar:2,Baz:3}"
      )

      invalid.foreach { input =>
        intercept[IllegalArgumentException] {
          parse(input)
        }
      }
    }

    "accept objects" in {
      codec.accepts(value) shouldBe true
      codec.accepts(Map(1 -> "Foo")) shouldBe false
    }
  }
}

class OnParMapCodecSpec
    extends AnyWordSpec
    with Matchers
    with CodecSpecBase[Map[String, String]]
    with OnParCodecSpec[Map[String, String], java.util.Map[String, String]] {

  "MapCodec" should {
    "on par with Java Codec (encode-decode)" in testEncodeDecode(
      null,
      Map(),
      Map("foo" -> "bar", "bar" -> "baz")
    )

    "on par with Java Codec (parse-format)" in testParseFormat(
      null,
      Map(),
      Map("foo" -> "bar", "bar" -> "baz")
    )
  }

  import scala.jdk.CollectionConverters._

  override protected val codec: TypeCodec[Map[String, String]] =
    MapCodec.frozen(TypeCodecs.TEXT, TypeCodecs.TEXT)

  override def javaCodec: TypeCodec[util.Map[String, String]] =
    TypeCodecs.mapOf(TypeCodecs.TEXT, TypeCodecs.TEXT)

  override def toJava(t: Map[String, String]): util.Map[String, String] =
    if (t == null) null else t.asJava
}
