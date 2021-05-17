package io.findify.featury.persistence.redis

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{FreqEstimator, FreqEstimatorSuite, StatsEstimator, StatsEstimatorSuite}

class RedisFreqEstimatorTest extends FreqEstimatorSuite with RedisMock {
  override def makeCounter(): Resource[IO, FreqEstimator] =
    Resource.make(IO(new RedisFreqEstimator(config, redisClient)))(_ => IO.unit)
}
