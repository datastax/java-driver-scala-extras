package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SetCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[Set[Int]] {

  override protected val codec: TypeCodec[Set[Int]] = SetCodec.frozen(IntCodec)

  "SetCodec" should {
    "encode" in {
      encode(null) shouldBe None
      encode(Set.empty[Int]) shouldBe Some("0x00000000")
      encode(Set(1, 2, 3)) shouldBe Some(
        "0x00000003000000040000000100000004000000020000000400000003"
      )
    }

    "decode" in {
      codec.decode(null, ProtocolVersion.DEFAULT) shouldBe Set.empty[Int]
      decode("0x00000000") shouldBe Some(Set.empty[Int])
      decode("0x00000003000000040000000100000004000000020000000400000003") shouldBe Some(
        Set(1, 2, 3)
      )
    }

    "format" in {
      format(Set()) shouldBe "{}"
      format(Set(1, 2, 3)) shouldBe "{1,2,3}"
    }

    "parse" in {
      parse("") shouldBe Set.empty[Int]
      parse("NULL") shouldBe Set.empty[Int]
      parse("{}") shouldBe Set.empty[Int]
      parse("{1,2,3}") shouldBe Set(1, 2, 3)
      parse(" { 1 , 2 , 3 } ") shouldBe Set(1, 2, 3)
    }

    "fail to parse invalid input" in {
      val invalid = Seq(
        "1,2,3}",
        "{1,2,3",
        "{{1,2,3}"
      )

      invalid.foreach { input =>
        intercept[IllegalArgumentException] {
          parse(input)
        }
      }
    }

    "accept objects" in {
      codec.accepts(Set(1, 2, 3)) shouldBe true
      codec.accepts(Set("foo", "bar")) shouldBe false
    }
  }
}
