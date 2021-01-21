package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class VectorCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[Vector[Int]] {

  override protected val codec: TypeCodec[Vector[Int]] = VectorCodec.frozen(IntCodec)

  "VectorCodec" should {
    "encode" in {
      encode(null) shouldBe None
      encode(Vector.empty[Int]) shouldBe Some("0x00000000")
      encode(Vector(1, 2, 3)) shouldBe Some(
        "0x00000003000000040000000100000004000000020000000400000003"
      )
    }

    "decode" in {
      decode(null) shouldBe Some(Vector.empty[Int])
      decode("0x00000000") shouldBe Some(Vector.empty[Int])
      decode("0x00000003000000040000000100000004000000020000000400000003") shouldBe Some(
        Vector(1, 2, 3)
      )
    }

    "format" in {
      format(Vector()) shouldBe "[]"
      format(Vector(1, 2, 3)) shouldBe "[1,2,3]"
    }

    "parse" in {
      parse("") shouldBe null
      parse("NULL") shouldBe null
      parse("[]") shouldBe Vector.empty[Int]
      parse("[1,2,3]") shouldBe Vector(1, 2, 3)
      parse(" [ 1 , 2 , 3 ] ") shouldBe Vector(1, 2, 3)
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
      codec.accepts(Vector(1, 2, 3)) shouldBe true
      codec.accepts(Vector("foo", "bar")) shouldBe false
    }
  }
}
