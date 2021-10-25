package io.findify.featury.connector.redis

import cats.effect.{IO, Resource}
import io.findify.featury.StoreTestSuite
import io.findify.featury.values.FeatureStore
import io.findify.featury.values.StoreCodec.ProtobufCodec
import io.findify.featury.values.ValueStoreConfig.RedisConfig
import redis.clients.jedis.Jedis
import redis.clients.jedis.args.FlushMode

class RedisStoreTest extends StoreTestSuite[RedisStore] {
  override def beforeAll = {
    super.beforeAll()
    val jedis = store.clientPool.pool.getResource
    jedis.flushAll(FlushMode.SYNC)
    store.clientPool.pool.returnResource(jedis)
  }
  override def storeResource =
    Resource.make(IO {
      RedisStore(RedisConfig("localhost", 6379, ProtobufCodec))
    })(redis => IO(redis.close()))
}
