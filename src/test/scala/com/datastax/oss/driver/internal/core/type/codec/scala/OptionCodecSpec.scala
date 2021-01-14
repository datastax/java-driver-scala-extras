package com.datastax.oss.driver.internal.core.`type`.codec.scala

import java.util.UUID

import com.datastax.oss.driver.api.core.`type`.DataTypes
import com.datastax.oss.driver.api.core.`type`.codec.{CodecNotFoundException, TypeCodec, TypeCodecs}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OptionCodecSpec
    extends AnyWordSpec
    with Matchers
    with CodecSpecBase[Option[Int]]
    with CassandraSpec {

  override protected val codec: TypeCodec[Option[Int]] = new OptionCodec[Int](IntCodec)

  private val optionStringCodec = new OptionCodec(TypeCodecs.TEXT)

  "BooleanCodec" should {
    "encode" in {
      encode(Some(1)) shouldBe Some("0x00000001")
      encode(None) shouldBe None
    }

    "decode" in {
      decode("0x00000001") shouldBe Some(Some(1))
      decode("0x") shouldBe Some(None)
    }

    "format" in {
      format(Some(1)) shouldBe "1"
      format(None) shouldBe "NULL"
    }

    "parse" in {
      parse("1") shouldBe Some(1)
      parse("NULL") shouldBe None
      parse("null") shouldBe None
      parse("") shouldBe None
      parse(null) shouldBe None
    }

    "fail to parse invalid input" in {
      intercept[IllegalArgumentException] {
        parse("maybe")
      }
    }

    "accept generic type" in {
      // TODO even if the 'Optional' codec doesn't go beyond this, would be nice to cover other Options
      //codec.accepts(GenericType.of(classOf[Option[_]])) shouldBe true
//      codec.accepts(GenericType.of(classOf[Option[Double]])) shouldBe false
//      codec.accepts(GenericType.of(classOf[Int])) shouldBe false
    }

    "accept raw type" in {
      codec.accepts(classOf[Option[Int]]) shouldBe true
//      codec.accepts(classOf[Option[Double]]) shouldBe false
//      codec.accepts(classOf[Int]) shouldBe false
    }

    "accept objects" in {
      codec.accepts(Some(1)) shouldBe true
      codec.accepts(None) shouldBe true
      codec.accepts(Int.MaxValue) shouldBe false
      codec.accepts(Some(2.0)) shouldBe false
    }

    "be queried correctly in the registry" in {
      val codecRegistry = session.getContext.getCodecRegistry

      codecRegistry.codecFor(codec.getJavaType) shouldBe codec
      codecRegistry.codecFor(optionStringCodec.getJavaType) shouldBe optionStringCodec

      codecRegistry.codecFor(DataTypes.INT, classOf[Option[_]]) shouldBe codec
      codecRegistry.codecFor(DataTypes.TEXT, classOf[Option[_]]) shouldBe optionStringCodec
      codecRegistry.codecFor(DataTypes.INT, None) shouldBe codec
      codecRegistry.codecFor(DataTypes.INT, Some(1)) shouldBe codec
      codecRegistry.codecFor(DataTypes.TEXT, None) shouldBe optionStringCodec
      codecRegistry.codecFor(DataTypes.TEXT, Some("foo")) shouldBe optionStringCodec

      intercept[CodecNotFoundException] {
        codecRegistry.codecFor(DataTypes.INT, Some("foo")) shouldBe optionStringCodec
      }
      intercept[CodecNotFoundException] {
        codecRegistry.codecFor(DataTypes.TEXT, Some(1)) shouldBe optionStringCodec
      }

      codecRegistry.codecFor(Some(1)) shouldBe codec
      codecRegistry.codecFor(Some("foo")) shouldBe optionStringCodec

      intercept[TestFailedException] {
        // This fails due to both codecs accepting the same value
        codecRegistry.codecFor(None) shouldBe codec
        codecRegistry.codecFor(None) shouldBe optionStringCodec
      }
    }

    "work with a table" in {
      withClue("when inserting nulls") {
        val id = UUID.randomUUID()
        session.execute(s"INSERT INTO option_table(id, opt_int, opt_str) VALUES ($id, null, null)")
        val rs = session.execute(s"SELECT * from option_table WHERE id = $id")

        rs.forEach { row =>
          row.get(0, classOf[UUID]) shouldBe id

          withClue("for class") {
            row.get(1, classOf[Option[_]]) shouldBe None
            row.get(2, classOf[Option[_]]) shouldBe None
          }

          withClue("for TypeCodec") {
            row.get(1, codec) shouldBe None
            row.get(2, optionStringCodec) shouldBe None
          }

          withClue("for GenericType") {
            row.get(1, codec.getJavaType) shouldBe None
            row.get(2, optionStringCodec.getJavaType) shouldBe None
          }

          withClue("fail when using the wrong codec") {
            intercept[CodecNotFoundException] {
              row.get(1, optionStringCodec.getJavaType) shouldBe None
            }
          }
        }
      }

      withClue("when inserting values") {
        val id = UUID.randomUUID()
        session.execute(s"INSERT INTO option_table(id, opt_int, opt_str) VALUES ($id, 42, 'foo')")
        val rs = session.execute(s"SELECT * from option_table WHERE id = $id")

        rs.forEach { row =>
          row.get(0, classOf[UUID]) shouldBe id
          val expectedInt    = Some(42)
          val expectedString = Some("foo")

          withClue("for class") {
            row.get(1, classOf[Option[_]]) shouldBe expectedInt
            row.get(2, classOf[Option[_]]) shouldBe expectedString
          }

          withClue("for TypeCodec") {
            row.get(1, codec) shouldBe expectedInt
            row.get(2, optionStringCodec) shouldBe expectedString
          }

          withClue("for GenericType") {
            row.get(1, codec.getJavaType) shouldBe expectedInt
            row.get(2, optionStringCodec.getJavaType) shouldBe expectedString
          }

          withClue("fail when using the wrong codec") {
            intercept[CodecNotFoundException] {
              row.get(1, optionStringCodec.getJavaType) shouldBe expectedInt
            }
          }
        }
      }
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    session.execute("""CREATE TABLE IF NOT EXISTS option_table(
        |   id         UUID,
        |   opt_int    INT,
        |   opt_str    TEXT,
        |   PRIMARY KEY (id)
        |)""".stripMargin)
    registerCodec(codec)
    registerCodec(optionStringCodec)
  }
}
