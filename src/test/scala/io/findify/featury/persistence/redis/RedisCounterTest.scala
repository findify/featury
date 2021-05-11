package io.findify.featury.persistence.redis

import cats.effect.{IO, Resource}
import com.github.fppt.jedismock.RedisServer
import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.feature.{Counter, CounterSuite}
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.persistence.memory.MemPersistence
import org.scalatest.BeforeAndAfterAll
import redis.clients.jedis.Jedis

class RedisCounterTest extends CounterSuite with BeforeAndAfterAll {
  val config                        = CounterConfig(FeatureName("counter"))
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

  override def makeCounter() =
    Resource.make(IO {
      val counter = new RedisCounter(config, redisClient)
      counter
    })(_ => IO.unit)
}
