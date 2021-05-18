package io.findify.featury.persistence.cassandra

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{StatsEstimator, StatsEstimatorSuite}

class CassandraStatsEstimatorTest extends StatsEstimatorSuite with CassandraClient {
  override def makeCounter(): Resource[IO, StatsEstimator] =
    CassandraPersistence.resource(cassandraConfig).evalMap(_.statsEstimator(config))
}
