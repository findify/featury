package io.findify.featury.persistence.cassandra

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{Counter, CounterSuite}

class CassandraCounterTest extends CounterSuite with CassandraClient {
  override def makeCounter(): Resource[IO, Counter] =
    CassandraPersistence.resource(cassandraConfig).evalMap(_.counter(config))
}
