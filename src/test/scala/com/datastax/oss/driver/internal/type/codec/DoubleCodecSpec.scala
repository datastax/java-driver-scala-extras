package com.datastax.oss.driver.internal.core.`type`.codec

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DoubleCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[Double] {

  override protected val codec: TypeCodec[Double] = DoubleCodec

  "DoubleCodec" should {
    "encode" in {
      encode(0.0) shouldBe Some("0x0000000000000000")
    }

    "decode" in {
      decode("0x0000000000000000") shouldBe Some(0.0)
      decode("0x") shouldBe Some(0.0)
    }

    "fail to decode if too many bytes" in {
      intercept[IllegalArgumentException] {
        decode("0x0000")
      }
    }

    "format" in {
      format(0.0) shouldBe "0.0"
    }

    "parse" in {
      parse("0.0") shouldBe 0.0
      parse("NULL") shouldBe 0.0
      parse("null") shouldBe 0.0
      parse("") shouldBe 0.0
      parse(null) shouldBe 0.0
    }

    "fail to parse invalid input" in {
      intercept[IllegalArgumentException] {
        parse("not a double")
      }
    }

    "accept generic type" in {
      codec.accepts(GenericType.of(classOf[Double])) shouldBe true
      codec.accepts(GenericType.of(classOf[Int])) shouldBe false
    }

    "accept raw type" in {
      codec.accepts(classOf[Double]) shouldBe true
      codec.accepts(classOf[Int]) shouldBe false
    }

    "accept objects" in {
      codec.accepts(123.45d) shouldBe true
      codec.accepts(Double.MaxValue) shouldBe true
      codec.accepts(Int.MaxValue) shouldBe false
    }
  }
}
