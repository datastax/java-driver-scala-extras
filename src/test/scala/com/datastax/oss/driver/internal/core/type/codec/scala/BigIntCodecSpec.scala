package com.datastax.oss.driver.internal.core.`type`.codec.scala

import java.math.BigInteger

import com.datastax.oss.driver.api.core.`type`.codec.{TypeCodec, TypeCodecs}
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BigIntCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[BigInt] with OnParCodecSpec[BigInt, java.math.BigInteger] {

  override protected val codec: TypeCodec[BigInt] = BigIntCodec

  "BigIntCodec" should {
    "encode" in {
      encode(BigInt(1)) shouldBe Some("0x01")
      encode(BigInt(128)) shouldBe Some("0x0080")
    }

    "decode" in {
      decode("0x01") shouldBe Some(BigInt(1))
      decode("0x0080") shouldBe Some(BigInt(128))
      // decode("0x") shouldBe None FIXME!
    }

    "format" in {
      format(BigInt(1)) shouldBe "1"
      format(null) shouldBe "NULL"
    }

    "parse" in {
      parse("1") shouldBe BigInt(1)
      parse("NULL") shouldBe null
      parse("null") shouldBe null
      parse("") shouldBe null
      parse(null) shouldBe null
    }

    "fail to parse invalid input" in {
      intercept[IllegalArgumentException] {
        parse("not a big int")
      }
    }

    "accept generic type" in {
      codec.accepts(GenericType.of(classOf[BigInt])) shouldBe true
      codec.accepts(GenericType.of(classOf[Float])) shouldBe false
    }

    "accept raw type" in {
      codec.accepts(classOf[BigInt]) shouldBe true
      codec.accepts(classOf[Float]) shouldBe false
    }

    "accept objects" in {
      codec.accepts(BigInt(123)) shouldBe true
      codec.accepts(Int.MaxValue) shouldBe false
    }

    "on par with Java Codec (encode-decode)" in testEncodeDecode(
      null,
      BigInt(0),
      BigInt(1),
      BigInt(123)
    )

    "on par with Java Codec (parse-format)" in testParseFormat(
      null,
      BigInt(0),
      BigInt(1),
      BigInt(123)
    )
  }

  override def javaCodec: TypeCodec[BigInteger] = TypeCodecs.VARINT

  override def toJava(t: BigInt): BigInteger = if (t == null) null else t.bigInteger
}
