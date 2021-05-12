package io.findify.featury.persistence.redis

import cats.effect.{IO, Resource}
import com.github.fppt.jedismock.RedisServer
import io.findify.featury.feature.ValuesSuite
import io.findify.featury.persistence.ValueStore
import io.findify.featury.persistence.memory.MemPersistence
import org.scalatest.BeforeAndAfterAll
import redis.clients.jedis.Jedis

class RedisValuesTest extends ValuesSuite with BeforeAndAfterAll {
  lazy val redisServer: RedisServer = RedisServer.newRedisServer(12345)
  lazy val redisClient              = new Jedis(redisServer.getHost, redisServer.getBindPort)

  override def beforeAll(): Unit = {
    super.beforeAll()
    redisServer.start()
  }

  override def afterAll(): Unit = {
    redisServer.stop()
    super.afterAll()
  }

  override def makeValues(): Resource[IO, ValueStore] = Resource.make(IO(new RedisValues(redisClient)))(_ => IO.unit)
}
