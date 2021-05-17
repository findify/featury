package io.findify.featury.persistence.cassandra

import cats.effect.unsafe.implicits.global
import com.datastax.oss.driver.api.core.CqlSession
import io.findify.featury.persistence.cassandra.CassandraPersistence.{CassandraConfig, Endpoint}
import org.scalatest.{BeforeAndAfterAll, Suite}

import java.net.InetSocketAddress

trait CassandraClient extends BeforeAndAfterAll { this: Suite =>
  val cassandraConfig = CassandraConfig(List(Endpoint("localhost")), "datacenter1", "dev")

  lazy val session = CassandraPersistence.makeSession(cassandraConfig).unsafeRunSync()

  override def beforeAll(): Unit = {
    super.beforeAll()
    session.execute(
      "create keyspace if not exists dev with replication = {'class': 'SimpleStrategy', 'replication_factor': 1}"
    )
  }

  override def afterAll(): Unit = {
    session.close()
    super.afterAll()
  }
}
