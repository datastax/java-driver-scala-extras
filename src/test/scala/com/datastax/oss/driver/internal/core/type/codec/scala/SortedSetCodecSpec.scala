package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.immutable.SortedSet

class SortedSetCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[SortedSet[Int]] {

  override protected val codec: TypeCodec[SortedSet[Int]] = SortedSetCodec.frozen(IntCodec)

  "SortedSetCodec" should {
    "encode" in {
      encode(null) shouldBe None
      encode(SortedSet.empty[Int]) shouldBe Some("0x00000000")
      encode(SortedSet(1, 2, 3)) shouldBe Some(
        "0x00000003000000040000000100000004000000020000000400000003"
      )
    }

    "decode" in {
      decode(null) shouldBe Some(Set.empty[Int])
      decode("0x00000000") shouldBe Some(SortedSet.empty[Int])
      decode("0x00000003000000040000000100000004000000020000000400000003") shouldBe Some(
        Set(1, 2, 3)
      )
    }

    "format" in {
      format(SortedSet()) shouldBe "{}"
      format(SortedSet(1, 2, 3)) shouldBe "{1,2,3}"
    }

    "parse" in {
      parse("") shouldBe null
      parse("NULL") shouldBe null
      parse("{}") shouldBe SortedSet.empty[Int]
      parse("{1,2,3}") shouldBe SortedSet(1, 2, 3)
      parse(" { 1 , 2 , 3 } ") shouldBe SortedSet(1, 2, 3)
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
      codec.accepts(SortedSet(1, 2, 3)) shouldBe true
      codec.accepts(SortedSet("foo", "bar")) shouldBe false
    }
  }
}
