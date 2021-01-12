package com.datastax.oss.driver.internal.core.`type`.codec

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ShortCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[Short] {

  override protected val codec: TypeCodec[Short] = ShortCodec

  "ShortCodec" should {
    val zero: Short = 0
    "encode" in {
      encode(zero) shouldBe Some("0x0000")
    }

    "decode" in {
      decode("0x0000") shouldBe Some(zero)
      decode("0x") shouldBe Some(zero)
    }

    "fail to decode if too many bytes" in {
      intercept[IllegalArgumentException] {
        decode("0x000000000")
      }
    }

    "format" in {
      format(zero) shouldBe "0"
    }

    "parse" in {
      parse("0") shouldBe zero
      parse("NULL") shouldBe zero
      parse("null") shouldBe zero
      parse("") shouldBe zero
      parse(null) shouldBe zero
    }

    "fail to parse invalid input" in {
      intercept[IllegalArgumentException] {
        parse("not an int")
      }
    }

    "accept generic type" in {
      codec.accepts(GenericType.of(classOf[Short])) shouldBe true
      codec.accepts(GenericType.of(classOf[Int])) shouldBe false
    }

    "accept raw type" in {
      codec.accepts(classOf[Short]) shouldBe true
      codec.accepts(classOf[Int]) shouldBe false
    }

    "accept objects" in {
      val oneTwoThree: Short = 123
      codec.accepts(oneTwoThree) shouldBe true
      codec.accepts(Int.MaxValue) shouldBe false
    }
  }
}
