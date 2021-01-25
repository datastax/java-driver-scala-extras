package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.DataTypes
import com.datastax.oss.driver.api.core.`type`.codec.registry.CodecRegistry
import com.datastax.oss.driver.api.core.`type`.codec.{TypeCodec, TypeCodecs}
import com.datastax.oss.driver.api.core.data.TupleValue
import com.datastax.oss.driver.api.core.detach.AttachmentPoint
import com.datastax.oss.driver.internal.core.`type`.DefaultTupleType
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TupleCodecSpec extends AnyWordSpec with Matchers with CodecSpecBase[(String, String)] {

  override protected val codec: TypeCodec[(String, String)] =
    TupleCodec.tuple2(TypeCodecs.TEXT, TypeCodecs.TEXT)

  "Tuple2Codec" should {
    "encode" in {
      encode(null) shouldBe None
      encode("foo" -> "bar") shouldBe Some("0x00000003666f6f00000003626172")
      encode("bar" -> "foo") shouldBe Some("0x0000000362617200000003666f6f")
      encode(null.asInstanceOf[String] -> "foo") shouldBe Some("0xffffffff00000003666f6f")
    }

    "decode" in {
      decode(null) shouldBe None
      decode("0x00000003666f6f00000003626172") shouldBe Some("foo" -> "bar")
      decode("0x0000000362617200000003666f6f") shouldBe Some("bar" -> "foo")
      decode("0xffffffff00000003666f6f") shouldBe Some(null.asInstanceOf[String] -> "foo")
    }

    "format" in {
      format("foo" -> "bar") shouldBe "('foo','bar')"
      format("foo" -> null.asInstanceOf[String]) shouldBe "('foo',NULL)"
    }

    "parse" in {
      parse("") shouldBe null
      parse("NULL") shouldBe null
      parse("('foo','bar')") shouldBe "foo" -> "bar"
      parse("(NULL,'bar')") shouldBe null.asInstanceOf[String] -> "bar"
      parse(" ( 'foo' , 'bar' ) ") shouldBe "foo" -> "bar"
    }

    "fail to parse invalid input" in {
      val invalid = Seq(
        "('foo')",
        "('foo','bar','baz')",
        "(('foo','bar')",
        "('foo','bar'",
        "('foo' 'bar')",
        "('foo', 123)",
        "('foo',"
      )

      invalid.foreach { input =>
        intercept[IllegalArgumentException] {
          parse(input)
        }
      }
    }

    "accept objects" in {
      codec.accepts("foo" -> "bar") shouldBe true
      codec.accepts("foo" -> 1) shouldBe false
      codec.accepts(List("foo", "bar")) shouldBe false
    }
  }
}

class OnParTupleCodecSpec
  extends AnyWordSpec
    with Matchers
    with CodecSpecBase[(String, Int)]
    with OnParCodecSpec[(String, Int), TupleValue]
    with BeforeAndAfterAll {

  import org.mockito.Mockito._
  import scala.jdk.CollectionConverters._

  private val attachmentPoint = mock(classOf[AttachmentPoint])
  private val codecRegistry = mock(classOf[CodecRegistry])
  private val tupleType = new DefaultTupleType(List(DataTypes.TEXT, DataTypes.INT).asJava, attachmentPoint)

  "TupleCodec" should {
    "on par with Java Codec (encode-decode)" in testEncodeDecode(
      null,
      "Foo" -> 1,
      "Bar" -> 42,
      "A Long string" -> 123
    )

    "on par with Java Codec (parse-format)" in testParseFormat(
      null,
      "Foo" -> 1,
      "Bar" -> 42,
      "A Long string" -> 123
    )
  }

  override protected val codec: TypeCodec[(String, Int)] = TupleCodec.tuple2(TypeCodecs.TEXT, IntCodec)

  override def javaCodec: TypeCodec[TupleValue] = TypeCodecs.tupleOf(tupleType)

  override def toJava(t: (String, Int)): TupleValue = if (t == null) null else {
    tupleType.newValue()
      .setString(0, t._1)
      .setInt(1, t._2)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()

    when(attachmentPoint.getCodecRegistry).thenReturn(codecRegistry)
    when(attachmentPoint.getProtocolVersion).thenReturn(ProtocolVersion.DEFAULT)

    // Called by the getters/setters
    when(codecRegistry.codecFor(DataTypes.INT, classOf[java.lang.Integer])).thenReturn(TypeCodecs.INT)
    when(codecRegistry.codecFor(DataTypes.TEXT, classOf[String])).thenReturn(TypeCodecs.TEXT)

    // Called by format/parse
    when(codecRegistry.codecFor[java.lang.Integer](DataTypes.INT)).thenReturn(TypeCodecs.INT)
    when(codecRegistry.codecFor[String](DataTypes.TEXT)).thenReturn(TypeCodecs.TEXT)
  }
}