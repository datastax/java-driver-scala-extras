package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LongCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[Long] {

  override protected val codec: TypeCodec[Long] = LongCodec

  "LongCodec" should {
    "encode" in {
      encode(0L) shouldBe Some("0x0000000000000000")
      encode(1L) shouldBe Some("0x0000000000000001")
    }

    "decode" in {
      decode("0x0000000000000000") shouldBe Some(0L)
      decode("0x0000000000000001") shouldBe Some(1L)
      decode("0x") shouldBe Some(0)
    }

    "fail to decode if too many bytes" in {
      intercept[IllegalArgumentException] {
        decode("0x0000")
      }
    }

    "format" in {
      format(0L) shouldBe "0"
      format(1L) shouldBe "1"
    }

    "parse" in {
      parse("0") shouldBe 0L
      parse("1") shouldBe 1L
      parse("NULL") shouldBe 0L
      parse("null") shouldBe 0L
      parse("") shouldBe 0L
      parse(null) shouldBe 0L
    }

    "fail to parse invalid input" in {
      intercept[IllegalArgumentException] {
        parse("not a long")
      }
    }

    "accept generic type" in {
      codec.accepts(GenericType.of(classOf[Long])) shouldBe true
      codec.accepts(GenericType.of(classOf[Float])) shouldBe false
    }

    "accept raw type" in {
      codec.accepts(classOf[Long]) shouldBe true
      codec.accepts(classOf[Float]) shouldBe false
    }

    "accept objects" in {
      codec.accepts(123L) shouldBe true
      codec.accepts(Long.MinValue) shouldBe true
      codec.accepts(Int.MinValue) shouldBe false
    }
  }
}