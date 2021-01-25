package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.codec.registry.MutableCodecRegistry
import javax.management.InstanceAlreadyExistsException
import org.apache.cassandra.config.DatabaseDescriptor
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{BeforeAndAfterAll, Suite}

trait CassandraSpec extends BeforeAndAfterAll { this: Suite =>

  def startTimeout: Long = EmbeddedCassandraServerHelper.DEFAULT_STARTUP_TIMEOUT * 3

  def session: CqlSession = EmbeddedCassandraServerHelper.getSession

  def keyspace: String = "tests"

  override def beforeAll(): Unit = {
    super.beforeAll()
    // Must have only one instance of Cassandra
    System.getSecurityManager.synchronized {
      // Cross-test tries to start Cassandra twice, but the objects are different
      if (Option(System.getProperty("cassandra-foreground")).isEmpty) {
        // Start embedded cassandra normally
        EmbeddedCassandraServerHelper.startEmbeddedCassandra(startTimeout)
      } else {
        try {
          // Use reflection to load the config into `DatabaseDescriptor`
          val config = DatabaseDescriptor.loadConfig()
          val m = classOf[DatabaseDescriptor].getDeclaredMethod("setConfig", classOf[org.apache.cassandra.config.Config])
          m.setAccessible(true)

          m.invoke(null, config)
          DatabaseDescriptor.applyAddressConfig()
        } catch {
          case e: RuntimeException if e.getCause.isInstanceOf[InstanceAlreadyExistsException] => // ignore
        }
      }

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
