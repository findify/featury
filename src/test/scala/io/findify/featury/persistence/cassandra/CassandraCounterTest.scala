package io.findify.featury.persistence.cassandra

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{Counter, CounterSuite}
import org.typelevel.log4cats.slf4j.Slf4jLogger

class CassandraCounterTest extends CounterSuite with CassandraClient {
  override def makeCounter(): Resource[IO, Counter] =
    CassandraPersistence.resource(cassandraConfig).evalMap(_.counter(config))
}
