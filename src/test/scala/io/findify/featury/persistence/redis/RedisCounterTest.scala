package io.findify.featury.persistence.redis

import cats.effect.{IO, Resource}
import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.feature.{Counter, CounterSuite}
import io.findify.featury.model.Key.FeatureName

class RedisCounterTest extends CounterSuite with RedisMock {
  val config = CounterConfig(FeatureName("counter"))

  override def makeCounter() =
    Resource.make(IO {
      val counter = new RedisCounter(config, redisClient)
      counter
    })(_ => IO.unit)
}
