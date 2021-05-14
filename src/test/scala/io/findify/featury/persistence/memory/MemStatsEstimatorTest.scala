package io.findify.featury.persistence.memory

import cats.effect.{IO, Resource}
import io.findify.featury.feature.StatsEstimator.StatsEstimatorConfig
import io.findify.featury.feature.{StatsEstimator, StatsEstimatorSuite}
import io.findify.featury.model.Key.FeatureName

class MemStatsEstimatorTest extends StatsEstimatorSuite {
  override def makeCounter(): Resource[IO, StatsEstimator] =
    Resource.make(IO(MemPersistence.statsEstimator(config)))(_ => IO.unit)
}
