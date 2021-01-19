package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.codec.{TypeCodec, TypeCodecs}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MapCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[Map[String, Int]] {

  override protected val codec: TypeCodec[Map[String, Int]] = MapCodec.frozen(TypeCodecs.TEXT, IntCodec)

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
      codec.decode(null, ProtocolVersion.DEFAULT) shouldBe Map.empty[String, Int]
      decode("0x00000000") shouldBe Some(Map.empty[String, Int])
      decode("0x0000000300000003466f6f00000004000000010000000342617200000004000000020000000342617a0000000400000003") shouldBe Some(
        value
      )
    }

    "format" in {
      format(Map.empty[String, Int]) shouldBe "{}"
      format(value) shouldBe "{'Foo':1,'Bar':2,'Baz':3}"
    }

    "parse" in {
      parse("") shouldBe Map.empty[String, Int]
      parse("NULL") shouldBe Map.empty[String, Int]
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
