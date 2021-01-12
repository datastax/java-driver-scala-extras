package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.codec.registry.MutableCodecRegistry
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{ BeforeAndAfterAll, Suite }

trait CassandraSpec extends BeforeAndAfterAll { this: Suite =>

  def startTimeout: Long = EmbeddedCassandraServerHelper.DEFAULT_STARTUP_TIMEOUT * 3

  def session: CqlSession = EmbeddedCassandraServerHelper.getSession

  def keyspace: String = "tests"

  override def beforeAll(): Unit = {
    super.beforeAll()
    // Must have only one instance of Cassandra
    System.getSecurityManager().synchronized {
      EmbeddedCassandraServerHelper.startEmbeddedCassandra(startTimeout)
      val session = EmbeddedCassandraServerHelper.getSession
  
      session.execute(
        s"CREATE KEYSPACE IF NOT EXISTS $keyspace WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1}"
      )
      session.execute(s"USE $keyspace")
    }
  }

  def registerCodec[T](codec: TypeCodec[T]): Unit =
    session.getContext.getCodecRegistry.asInstanceOf[MutableCodecRegistry].register(codec)
}
