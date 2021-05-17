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

class RedisTextBoundedListTest extends BoundedListSuite[Text] with RedisClient {

  override def contentType: FeatureValue.ScalarType = TextType
  override def makeList(config: BoundedListConfig): Resource[IO, BoundedList[Text]] =
    Resource.make(IO(new RedisTextBoundedList(redisClient, config)))(_ => IO.unit)

  override def makeValue(i: Int): Text = Text(i.toString)
}
