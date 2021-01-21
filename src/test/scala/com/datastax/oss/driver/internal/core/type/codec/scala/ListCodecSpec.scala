package com.datastax.oss.driver.internal.core.`type`.codec.scala

import java.util

import com.datastax.oss.driver.api.core.`type`.codec.{ TypeCodec, TypeCodecs }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ListCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[List[Int]] {

  override protected val codec: TypeCodec[List[Int]] = ListCodec.frozen(IntCodec)

  "ListCodec" should {
    "encode" in {
      encode(null) shouldBe None
      encode(List.empty[Int]) shouldBe Some("0x00000000")
      encode(List(1, 2, 3)) shouldBe Some(
        "0x00000003000000040000000100000004000000020000000400000003"
      )
    }

    "decode" in {
      decode(null) shouldBe Some(List.empty[Int])
      decode("0x00000000") shouldBe Some(List.empty[Int])
      decode("0x00000003000000040000000100000004000000020000000400000003") shouldBe Some(
        List(1, 2, 3)
      )
    }

    "format" in {
      format(List()) shouldBe "[]"
      format(List(1, 2, 3)) shouldBe "[1,2,3]"
    }

    "parse" in {
      parse("") shouldBe null
      parse("NULL") shouldBe null
      parse("[]") shouldBe List.empty[Int]
      parse("[1,2,3]") shouldBe List(1, 2, 3)
      parse(" [ 1 , 2 , 3 ] ") shouldBe List(1, 2, 3)
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
      codec.accepts(List(1, 2, 3)) shouldBe true
      codec.accepts(List("foo", "bar")) shouldBe false
    }
  }

}

class OnParListCodecSpec
    extends AnyWordSpec
    with Matchers
    with CodecSpecBase[List[String]]
    with OnParCodecSpec[List[String], java.util.List[String]] {

  "ListCodec" should {
    "on par with Java Codec (encode-decode)" in testEncodeDecode(
      null,
      List(),
      List("foo", "bar")
    )

    "on par with Java Codec (parse-format)" in testParseFormat(
      null,
      List(),
      List("foo", "bar")
    )
  }

  import scala.jdk.CollectionConverters._

  override protected val codec: TypeCodec[List[String]] = ListCodec.frozen(TypeCodecs.TEXT)

  override def javaCodec: TypeCodec[util.List[String]] = TypeCodecs.listOf(TypeCodecs.TEXT)

  override def toJava(t: List[String]): util.List[String] = if (t == null) null else t.asJava
}
