package com.datastax.oss.driver.internal.core.`type`.codec.scala

import java.util

import com.datastax.oss.driver.api.core.`type`.codec.{TypeCodec, TypeCodecs}
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
      decode(null) shouldBe Some(Set.empty[Int])
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
      parse("") shouldBe null
      parse("NULL") shouldBe null
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

class OnParSetCodecSpec
  extends AnyWordSpec
    with Matchers
    with CodecSpecBase[Set[String]]
    with OnParCodecSpec[Set[String], java.util.Set[String]] {

  "SetCodec" should {
    "on par with Java Codec (encode-decode)" in testEncodeDecode(
      null,
      Set(),
      Set("foo", "bar")
    )

    "on par with Java Codec (parse-format)" in testParseFormat(
      null,
      Set(),
      Set("foo", "bar")
    )
  }

  import scala.jdk.CollectionConverters._

  override protected val codec: TypeCodec[Set[String]] = SetCodec.frozen(TypeCodecs.TEXT)

  override def javaCodec: TypeCodec[util.Set[String]] = TypeCodecs.setOf(TypeCodecs.TEXT)

  override def toJava(t: Set[String]): util.Set[String] = if (t == null) null else t.asJava
}