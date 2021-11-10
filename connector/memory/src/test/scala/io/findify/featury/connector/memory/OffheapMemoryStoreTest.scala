package io.findify.featury.connector.memory

import cats.effect.{IO, Resource}
import io.findify.featury.StoreTestSuite
import io.findify.featury.connector.redis.RedisStore
import io.findify.featury.values.StoreCodec.ProtobufCodec
import io.findify.featury.values.ValueStoreConfig.RedisConfig
import redis.clients.jedis.args.FlushMode

import scala.util.Random

class OffheapMemoryStoreTest extends StoreTestSuite[RedisStore] {
  var server: OffheapMemoryStore = _
  override def beforeAll = {
    super.beforeAll()
    server = OffheapMemoryStore.startUnsafe(16379)
    val jedis = store.clientPool.pool.getResource
    jedis.flushAll(FlushMode.SYNC)
    store.clientPool.pool.returnResource(jedis)
  }

  override def afterAll(): Unit = {
    OffheapMemoryStore.stopUnsafe(server)
    super.afterAll()
  }
  override def storeResource =
    Resource.make(IO {
      RedisStore(RedisConfig("localhost", 16379, ProtobufCodec))
    })(redis => IO(redis.close()))

  it should "start and stop server" in {
    val server = OffheapMemoryStore.startUnsafe(1024 + Random.nextInt(50000))
    OffheapMemoryStore.stopUnsafe(server)
  }
}
