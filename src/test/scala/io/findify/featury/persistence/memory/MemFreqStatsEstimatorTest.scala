package io.findify.featury.persistence.memory

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{FreqEstimator, FreqEstimatorSuite, StatsEstimator, StatsEstimatorSuite}

class MemFreqStatsEstimatorTest extends FreqEstimatorSuite {
  override def makeCounter(): Resource[IO, FreqEstimator] =
    Resource.make(IO(MemPersistence.freqEstimator(config)))(_ => IO.unit)

}
