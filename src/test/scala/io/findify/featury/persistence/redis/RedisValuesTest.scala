package io.findify.featury.persistence.redis

import cats.effect.{IO, Resource}
import io.findify.featury.feature.ValuesSuite
import io.findify.featury.persistence.ValueStore

class RedisValuesTest extends ValuesSuite with RedisMock {
  override def makeValues(): Resource[IO, ValueStore] = Resource.make(IO(new RedisValues(redisClient)))(_ => IO.unit)
}
