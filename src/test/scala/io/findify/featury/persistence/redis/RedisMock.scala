package io.findify.featury.persistence.redis

import org.scalatest.{BeforeAndAfterAll, Suite}
import redis.clients.jedis.Jedis

trait RedisMock extends BeforeAndAfterAll { this: Suite =>
  lazy val redisClient = new Jedis("localhost", 6379)

  override def afterAll(): Unit = {
    redisClient.close()
    super.afterAll()
  }
}
