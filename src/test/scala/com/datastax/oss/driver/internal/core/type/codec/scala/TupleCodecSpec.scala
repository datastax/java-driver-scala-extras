package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.{ TypeCodec, TypeCodecs }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TupleCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[(String, String)] {

  override protected val codec: TypeCodec[(String, String)] =
    TupleCodec.tuple2(TypeCodecs.TEXT, TypeCodecs.TEXT)

  "Tuple2Codec" should {
    "encode" in {
      encode(null) shouldBe None
      encode("foo" -> "bar") shouldBe Some("0x00000003666f6f00000003626172")
      encode("bar" -> "foo") shouldBe Some("0x0000000362617200000003666f6f")
      encode(null.asInstanceOf[String] -> "foo") shouldBe Some("0xffffffff00000003666f6f")
    }

    "decode" in {
      decode(null) shouldBe None
      decode("0x00000003666f6f00000003626172") shouldBe Some("foo" -> "bar")
      decode("0x0000000362617200000003666f6f") shouldBe Some("bar" -> "foo")
      decode("0xffffffff00000003666f6f") shouldBe Some(null.asInstanceOf[String] -> "foo")
    }

    "format" in {
      format("foo" -> "bar") shouldBe "('foo','bar')"
      format("foo" -> null.asInstanceOf[String]) shouldBe "('foo',NULL)"
    }

    "parse" in {
      parse("") shouldBe null
      parse("NULL") shouldBe null
      parse("('foo','bar')") shouldBe "foo" -> "bar"
      parse("(NULL,'bar')") shouldBe null.asInstanceOf[String] -> "bar"
      parse(" ( 'foo' , 'bar' ) ") shouldBe "foo" -> "bar"
    }

    "fail to parse invalid input" in {
      val invalid = Seq(
        "('foo')",
        "('foo','bar','baz')",
        "(('foo','bar')",
        "('foo','bar'",
        "('foo' 'bar')",
        "('foo', 123)",
        "('foo',"
      )

      invalid.foreach { input =>
        intercept[IllegalArgumentException] {
          parse(input)
        }
      }
    }

    "accept objects" in {
      codec.accepts("foo" -> "bar") shouldBe true
      codec.accepts("foo" -> 1) shouldBe false
      codec.accepts(List("foo", "bar")) shouldBe false
    }
  }
}
