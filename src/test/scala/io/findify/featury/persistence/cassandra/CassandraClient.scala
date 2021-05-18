package io.findify.featury.persistence.cassandra

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.findify.featury.persistence.cassandra.CassandraPersistence.{CassandraConfig, Endpoint}
import org.scalatest.{BeforeAndAfterAll, Suite}
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait CassandraClient extends BeforeAndAfterAll { this: Suite =>
  implicit lazy val logger = Slf4jLogger.getLogger[IO]
  val cassandraConfig      = CassandraConfig(List(Endpoint("localhost")), "datacenter1", "dev")

  lazy val session = CassandraPersistence.makeSession(cassandraConfig).unsafeRunSync()

  override def beforeAll(): Unit = {
    super.beforeAll()
    session.execute("drop keyspace if exists dev")
    session.execute(
      "create keyspace if not exists dev with replication = {'class': 'SimpleStrategy', 'replication_factor': 1}"
    )
  }

  override def afterAll(): Unit = {
    session.close()
    super.afterAll()
  }
}
