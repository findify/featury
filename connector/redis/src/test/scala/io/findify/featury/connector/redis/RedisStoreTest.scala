package io.findify.featury.connector.redis

import cats.effect.{IO, Resource}
import io.findify.featury.StoreTestSuite
import io.findify.featury.values.StoreCodec.ProtobufCodec
import io.findify.featury.values.ValueStoreConfig.RedisConfig
import redis.clients.jedis.args.FlushMode

class RedisStoreTest extends StoreTestSuite[RedisStore] {
  override def beforeAll() = {
    super.beforeAll()
    val jedis = store.client
    jedis.flushAll(FlushMode.SYNC)
  }
  override def storeResource =
    Resource.make(IO {
      RedisStore(RedisConfig("localhost", 6379, ProtobufCodec))
    })(redis => IO(redis.close()))
}
