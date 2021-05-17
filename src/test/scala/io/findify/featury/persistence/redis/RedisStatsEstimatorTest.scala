package io.findify.featury.persistence.redis

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{StatsEstimator, StatsEstimatorSuite}

class RedisStatsEstimatorTest extends StatsEstimatorSuite with RedisClient {
  override def makeCounter(): Resource[IO, StatsEstimator] =
    Resource.make(IO(new RedisStatsEstimator(config, redisClient)))(_ => IO.unit)
}
