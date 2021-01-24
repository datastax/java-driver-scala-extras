package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.{TypeCodec, TypeCodecs}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TupleCodecsSpec extends AnyWordSpec with Matchers {

  "Tuple2Codec (String, Int)" should beAValidTupleCodec(
    TupleCodec.tuple2(TypeCodecs.TEXT, IntCodec))(
    "foo" -> 1,
    "bar" -> 3,
    "some long text" -> 4
  )

  "Tuple2Codec (Int, Double)" should beAValidTupleCodec(
    TupleCodec.tuple2(IntCodec, DoubleCodec))(
    1 -> 2.0,
    3 -> 4.55,
    5 -> 123.456
  )

  "Tuple3Codec (String, Int, String)" should beAValidTupleCodec(
    TupleCodec.tuple3(TypeCodecs.TEXT, IntCodec, TypeCodecs.TEXT))(
    ("foo", 1, "bar"),
    ("bar", 3, "foo"),
    ("some long text", 4, "")
  )

  "Tuple4Codec (Int, Double, String, String)" should beAValidTupleCodec(
    TupleCodec.tuple4(IntCodec, DoubleCodec, TypeCodecs.TEXT, TypeCodecs.TEXT))(
    (1, 2.0, "foo", "bar"),
    (3, 4.55, "tar", "baz"),
    (5, 123.456, "some long text", "")
  )



  def beAValidTupleCodec[T <: Product](c: TypeCodec[T])(testData: T*): Unit = {
    val base = new CodecSpecBase[T] {
      override val codec: TypeCodec[T] = c
    }

    "encode and decode" in {
      base.encode(null.asInstanceOf[T]) shouldBe None
      base.decode(null) shouldBe None

      testData.foreach { value =>
        val encoded = base.encode(value)
        encoded shouldBe defined

        base.decode(encoded.get) shouldBe Some(value)
      }
    }

    "format and parse" in {
      base.format(null.asInstanceOf[T]) shouldBe "NULL"
      base.parse("").asInstanceOf[AnyRef] shouldBe null
      base.parse("NULL").asInstanceOf[AnyRef] shouldBe null

      testData.foreach { value =>
        base.parse(base.format(value)) shouldBe value
      }
    }

    "accept objects" in {
      testData.foreach { value =>
        c.accepts(value) shouldBe true
      }
    }
  }

}
