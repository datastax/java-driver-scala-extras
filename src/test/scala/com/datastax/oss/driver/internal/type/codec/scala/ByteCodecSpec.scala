package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ByteCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[Byte] {

  override protected val codec: TypeCodec[Byte] = ByteCodec

  "ByteCodec" should {
    val zero: Byte = 0

    "encode" in {
      encode(zero) shouldBe Some("0x00")
    }

    "decode" in {
      decode("0x00") shouldBe Some(zero)
      decode("0x") shouldBe Some(zero)
    }

    "fail to decode if too many bytes" in {
      intercept[IllegalArgumentException] {
        decode("0x0000")
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
        parse("not a byte")
      }
    }

    "accept generic type" in {
      codec.accepts(GenericType.of(classOf[Byte])) shouldBe true
      codec.accepts(GenericType.of(classOf[Int])) shouldBe false
    }

    "accept raw type" in {
      codec.accepts(classOf[Byte]) shouldBe true
      codec.accepts(classOf[Int]) shouldBe false
    }

    "accept objects" in {
      val oneTwoThree: Byte = 123
      codec.accepts(oneTwoThree) shouldBe true
      codec.accepts(Byte.MaxValue) shouldBe true
      codec.accepts(Int.MaxValue) shouldBe false
    }
  }
}
