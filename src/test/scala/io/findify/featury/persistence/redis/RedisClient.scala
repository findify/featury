package io.findify.featury.persistence.redis

import org.scalatest.{BeforeAndAfterAll, Suite}
import redis.clients.jedis.Jedis

trait RedisClient extends BeforeAndAfterAll { this: Suite =>
  lazy val redisClient = new Jedis("localhost", 6379)

  override def beforeAll(): Unit = {
    super.beforeAll()
    redisClient.flushAll()
  }

  override def afterAll(): Unit = {
    redisClient.close()
    super.afterAll()
  }
}
