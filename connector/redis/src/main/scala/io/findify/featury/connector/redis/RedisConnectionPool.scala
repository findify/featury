package io.findify.featury.connector.redis

import cats.effect.IO
import cats.effect.kernel.Resource
import redis.clients.jedis.JedisPool

import java.net.URI
import scala.util.Try

case class RedisConnectionPool(pool: JedisPool) {
  def client = Resource.make(IO.fromTry(Try(pool.getResource)))(c => IO.fromTry(Try(pool.returnResource(c))))
}

object RedisConnectionPool {
  def apply(host: String, port: Int, timeout: Int = 2000) =
    new RedisConnectionPool(new JedisPool(URI.create(s"redis://$host:$port"), timeout))
}
