package com.datastax.driver.extras.codecs.scala

import com.datastax.driver.core._
import org.scalatest.prop.PropertyChecks
import org.scalatest.tagobjects.{Network, Slow}
import org.scalatest.{BeforeAndAfterAll, Matchers, PropSpec}

import scala.util.Random

/**
  *
  */
class CodecDbSpec extends PropSpec with PropertyChecks with Matchers with BeforeAndAfterAll {

  val keyspace: String = "ks_" + Random.alphanumeric.take(10).mkString("").toLowerCase

  var cluster: Cluster = _
  var session: Session = _

  override def beforeAll(): Unit = {
    cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    session = cluster.connect()
    session.execute(s"""CREATE KEYSPACE $keyspace WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }""")
  }

  override def afterAll(): Unit = {
    session.execute(s"""DROP KEYSPACE $keyspace""")
    cluster.close()
  }

  def withDbFixture(dataType: DataType, testCode: (PreparedStatement, PreparedStatement) => Any) {
    val table: String = "table_" + Random.alphanumeric.take(10).mkString("").toLowerCase
    session.execute(s"""CREATE TABLE $keyspace.$table (pk int PRIMARY KEY, c $dataType)""")
    val insert = session.prepare(s"""INSERT INTO $keyspace.$table (pk, c) VALUES (0,?)""")
    val select = session.prepare(s"""SELECT c FROM $keyspace.$table WHERE pk=0""")
    try {
      testCode(insert, select)
    } finally {
      session.execute(s"""TRUNCATE $keyspace.$table""")
    }
  }

  def storeAndRetrieve[T](insert: PreparedStatement, select: PreparedStatement, n: T, codec: TypeCodec[T]) {
    session.execute(insert.bind().set(0, n, codec))
    session.execute(select.bind()).one.get(0, codec) should equal(n)
    session.execute(insert.bind().set(0, n, codec.getJavaType))
    session.execute(select.bind()).one.get(0, codec.getJavaType) should equal(n)
  }

  property("Boolean values should be stored and retrieved from a CQL int table column", Slow, Network) {
    withDbFixture(DataType.cboolean(), { (insert, select) =>
      forAll { (n: Boolean) =>
        storeAndRetrieve(insert, select, n, BooleanCodec)
      }
    })
  }

  property("Int values should be stored and retrieved from a CQL int table column", Slow, Network) {
    withDbFixture(DataType.cint(), { (insert, select) =>
      forAll { (n: Int) =>
        storeAndRetrieve(insert, select, n, IntCodec)
      }
    })
  }

  property("BigDecimal values should be stored and retrieved from a CQL int table column", Slow, Network) {
    withDbFixture(DataType.decimal(), { (insert, select) =>
      cluster.getConfiguration.getCodecRegistry.register(BigDecimalCodec)
      forAll { (n: BigDecimal) =>
        storeAndRetrieve(insert, select, n, BigDecimalCodec)
      }
    })
  }

  property("Option[Int] values should be stored and retrieved from a CQL int table column", Slow, Network) {
    withDbFixture(DataType.cint(), { (insert, select) =>
      cluster.getConfiguration.getCodecRegistry.register(OptionCodec[Int])
      forAll { (n: Option[Int]) =>
        storeAndRetrieve(insert, select, n, OptionCodec[Int])
      }
    })
  }

  property("Option[String] values should be stored and retrieved from a CQL int table column", Slow, Network) {
    withDbFixture(DataType.varchar(), { (insert, select) =>
      cluster.getConfiguration.getCodecRegistry.register(OptionCodec[String])
      forAll { (n: Option[String]) =>
        storeAndRetrieve(insert, select, n, OptionCodec[String])
      }
    })
  }

  property("Seq[Int] values should be stored and retrieved from a CQL int table column", Slow, Network) {
    withDbFixture(DataType.list(DataType.cint()), { (insert, select) =>
      cluster.getConfiguration.getCodecRegistry.register(SeqCodec[Int])
      forAll { (n: Seq[Int]) =>
        storeAndRetrieve(insert, select, n, SeqCodec[Int])
      }
    })
  }

  property("Map[Option[Int],Seq[Set[Tuple2[String,Boolean]]]] values should be stored and retrieved from a CQL int table column", Slow, Network) {
    val tupleType = cluster.getMetadata.newTupleType(DataType.varchar(), DataType.cboolean())
    val fixtureType = DataType.map(DataType.cint(), DataType.list(DataType.set(tupleType, true), true))
    val codec = MapCodec[Option[Int], Seq[Set[(String, Boolean)]]]
    cluster.getConfiguration.getCodecRegistry.register(codec)
    withDbFixture(fixtureType, { (insert, select) =>
      forAll { (n: Map[Option[Int], Seq[Set[(String, Boolean)]]]) =>
        storeAndRetrieve(insert, select, n, codec)
      }
    })
  }

}
