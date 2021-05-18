package io.findify.featury.persistence.cassandra

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{FreqEstimator, FreqEstimatorSuite}

class CassandraFreqEstimatorTest extends FreqEstimatorSuite with CassandraClient {
  override def makeCounter(): Resource[IO, FreqEstimator] =
    CassandraPersistence.resource(cassandraConfig).evalMap(_.freqEstimator(config))
}
