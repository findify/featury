package io.findify.featury.persistence.redis

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{PeriodicCounter, PeriodicCounterSuite}

class RedisPeriodicCounterTest extends PeriodicCounterSuite with RedisClient {
  override def makeCounter(): Resource[IO, PeriodicCounter] =
    Resource.make(IO(new RedisPeriodicCounter(config, redisClient)))(_ => IO.unit)
}
