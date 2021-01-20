package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BigDecimalCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[BigDecimal] {

  override protected val codec: TypeCodec[BigDecimal] = BigDecimalCodec

  "BigDecimalCodec" should {
    "encode" in {
      encode(BigDecimal(1)) shouldBe Some(
        "0x"
        + "00000000" // scale
        + "01" // unscaled value
      )
      encode(BigDecimal(128, 4)) shouldBe Some(
        "0x"
        + "00000004" // scale
        + "0080" // unscaled value
      )
    }

    "decode" in {
      decode("0x0000000001") shouldBe Some(BigDecimal(1))
      decode("0x000000040080") shouldBe Some(BigDecimal(128, 4))
      // decode("0x") shouldBe None FIXME!
    }

    "fail to decode if not enough bytes" in {
      intercept[IllegalArgumentException] {
        decode("0x0000")
      }
    }

    "format" in {
      format(BigDecimal(1)) shouldBe "1"
      format(BigDecimal(128, 4)) shouldBe "0.0128"
      format(null) shouldBe "NULL"
    }

    "parse" in {
      parse("1") shouldBe BigDecimal(1)
      parse("0.0128") shouldBe BigDecimal(128, 4)
      parse("null") shouldBe null
      parse("") shouldBe null
      parse(null) shouldBe null
    }

    "fail to parse invalid input" in {
      intercept[IllegalArgumentException] {
        parse("not a decimal")
      }
    }

    "accept generic type" in {
      codec.accepts(GenericType.of(classOf[BigDecimal])) shouldBe true
      codec.accepts(GenericType.of(classOf[Float])) shouldBe false
    }

    "accept raw type" in {
      codec.accepts(classOf[BigDecimal]) shouldBe true
      codec.accepts(classOf[Float]) shouldBe false
    }

    "accept objects" in {
      codec.accepts(BigDecimal(123)) shouldBe true
      codec.accepts(Double.MaxValue) shouldBe false
    }
  }
}
