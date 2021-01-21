package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SeqCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[Seq[Int]] {

  override protected val codec: TypeCodec[Seq[Int]] = SeqCodec.frozen(IntCodec)

  "SeqCodec" should {
    "encode" in {
      encode(null) shouldBe None
      encode(Seq.empty[Int]) shouldBe Some("0x00000000")
      encode(Seq(1, 2, 3)) shouldBe Some(
        "0x00000003000000040000000100000004000000020000000400000003"
      )
    }

    "decode" in {
      decode(null) shouldBe Some(Seq.empty[Int])
      decode("0x00000000") shouldBe Some(Seq.empty[Int])
      decode("0x00000003000000040000000100000004000000020000000400000003") shouldBe Some(
        Seq(1, 2, 3)
      )
    }

    "format" in {
      format(Seq()) shouldBe "[]"
      format(Seq(1, 2, 3)) shouldBe "[1,2,3]"
    }

    "parse" in {
      parse("") shouldBe null
      parse("NULL") shouldBe null
      parse("[]") shouldBe Seq.empty[Int]
      parse("[1,2,3]") shouldBe Seq(1, 2, 3)
      parse(" [ 1 , 2 , 3 ] ") shouldBe Seq(1, 2, 3)
    }

    "fail to parse invalid input" in {
      val invalid = Seq(
        "1,2,3]",
        "[1,2,3",
        "[[1,2,3]"
      )

      invalid.foreach { input =>
        intercept[IllegalArgumentException] {
          parse(input)
        }
      }
    }

    "accept objects" in {
      codec.accepts(Seq(1, 2, 3)) shouldBe true
      codec.accepts(Seq("foo", "bar")) shouldBe false
    }
  }
}
