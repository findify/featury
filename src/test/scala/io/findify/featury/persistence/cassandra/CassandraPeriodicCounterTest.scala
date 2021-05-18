package io.findify.featury.persistence.cassandra

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{PeriodicCounter, PeriodicCounterSuite}

class CassandraPeriodicCounterTest extends PeriodicCounterSuite with CassandraClient {
  override def makeCounter(): Resource[IO, PeriodicCounter] =
    CassandraPersistence.resource(cassandraConfig).evalMap(_.periodicCounter(config))
}
