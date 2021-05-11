package io.findify.featury.persistence.redis

import cats.effect.{IO, Resource}
import com.github.fppt.jedismock.RedisServer
import io.findify.featury.feature.BoundedList.BoundedListConfig
import io.findify.featury.feature.{BoundedList, BoundedListSuite}
import io.findify.featury.model.FeatureValue
import io.findify.featury.model.FeatureValue.{Text, TextType}
import io.findify.featury.persistence.memory.MemPersistence
import io.findify.featury.persistence.redis.RedisBoundedList.RedisTextBoundedList
import org.scalatest.BeforeAndAfterAll
import redis.clients.jedis.Jedis

class RedisTextBoundedListTest extends BoundedListSuite[Text] with BeforeAndAfterAll {
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

  override def contentType: FeatureValue.ScalarType = TextType
  override def makeList(config: BoundedListConfig): Resource[IO, BoundedList[Text]] =
    Resource.make(IO(new RedisTextBoundedList(redisClient, config)))(_ => IO.unit)

  override def makeValue(i: Int): Text = Text(i.toString)
}
