package com.datastax.driver.extras.codecs.scala

import com.datastax.driver.core._
import com.datastax.driver.extras.codecs.scala.Implicits._
import org.scalatest.prop.PropertyChecks
import org.scalatest.tagobjects.{Network, Slow}
import org.scalatest.{Matchers, PropSpec}

import scala.util.Random

/**
  *
  */
class ImplicitConversionsSpec extends PropSpec with PropertyChecks with Matchers {

  def withDbFixture(testCode: (Session, PreparedStatement, PreparedStatement) => Any) {
    val keyspace: String = "ks_" + Random.alphanumeric.take(10).mkString("").toLowerCase
    val table: String = "table_" + Random.alphanumeric.take(10).mkString("").toLowerCase
    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val session = cluster.connect()
    session.execute(s"""CREATE KEYSPACE $keyspace WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }""")
    val codec = MapCodec[Option[Int], Seq[(String, BigDecimal)]]
    cluster.getConfiguration.getCodecRegistry.register(codec)
    session.execute(s"""CREATE TABLE $keyspace.$table (pk int PRIMARY KEY, c map<int, frozen<list<tuple<varchar, decimal>>>>)""")
    val insert = session.prepare(s"""INSERT INTO $keyspace.$table (pk, c) VALUES (0,?)""")
    val select = session.prepare(s"""SELECT c FROM $keyspace.$table WHERE pk=0""")
    try {
      testCode(session, insert, select)
    } finally {
      session.execute(s"""TRUNCATE $keyspace.$table""")
      session.execute(s"""DROP KEYSPACE $keyspace""")
      cluster.close()
    }
  }

  property("Values should be stored and retrieved from a CQL int table column using implicits", Slow, Network) {
    withDbFixture { (session, insert, select) =>
      forAll { (expected: Map[Option[Int], Seq[(String, BigDecimal)]]) =>
        val statement: BoundStatement = insert.bind()
        statement.setImplicitly(0, expected)
        session.execute(statement)
        val row = session.execute(select.bind()).one
        val actual = row.getImplicitly[Map[Option[Int], Seq[(String, BigDecimal)]]](0)
        actual should equal(expected)
      }
    }
  }

}
