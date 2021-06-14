package io.findify.featury.connector.redis

import cats.effect.{IO, Resource}
import io.findify.featury.StoreTestSuite
import io.findify.featury.values.FeatureStore
import io.findify.featury.values.StoreCodec.ProtobufCodec
import redis.clients.jedis.Jedis

class RedisStoreTest extends StoreTestSuite {
  override def storeResource: Resource[IO, FeatureStore] =
    Resource.make(IO(RedisStore(new Jedis("localhost", 6379), ProtobufCodec)))(redis => IO(redis.client.close()))
}
