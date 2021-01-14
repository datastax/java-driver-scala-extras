package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class IntCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[Int] {

  override protected val codec: TypeCodec[Int] = IntCodec

  "IntCodec" should {
    "encode" in {
      encode(0) shouldBe Some("0x00000000")
    }

    "decode" in {
      decode("0x00000000") shouldBe Some(0)
      decode("0x") shouldBe Some(0)
    }

    "fail to decode if too many bytes" in {
      intercept[IllegalArgumentException] {
        decode("0x0000")
      }
    }

    "format" in {
      format(0) shouldBe "0"
    }

    "parse" in {
      parse("0") shouldBe 0
      parse("NULL") shouldBe 0
      parse("null") shouldBe 0
      parse("") shouldBe 0
      parse(null) shouldBe 0
    }

    "fail to parse invalid input" in {
      intercept[IllegalArgumentException] {
        parse("not an int")
      }
    }

    "accept generic type" in {
      codec.accepts(GenericType.of(classOf[Int])) shouldBe true
      codec.accepts(GenericType.of(classOf[Float])) shouldBe false
    }

    "accept raw type" in {
      codec.accepts(classOf[Int]) shouldBe true
      codec.accepts(classOf[Float]) shouldBe false
    }

    "accept objects" in {
      codec.accepts(123) shouldBe true
      codec.accepts(Int.MaxValue) shouldBe true
    }
  }
}