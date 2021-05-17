package io.findify.featury.persistence.redis

import cats.effect.{IO, Resource}
import com.github.fppt.jedismock.RedisServer
import io.findify.featury.feature.BoundedList.BoundedListConfig
import io.findify.featury.feature.{BoundedList, BoundedListSuite}
import io.findify.featury.model.FeatureValue
import io.findify.featury.model.FeatureValue.{Num, Text, TextType}
import io.findify.featury.persistence.redis.RedisBoundedList.{RedisNumBoundedList, RedisTextBoundedList}
import org.scalatest.BeforeAndAfterAll
import redis.clients.jedis.Jedis

class RedisNumBoundedListTest extends BoundedListSuite[Num] with RedisClient {
  override def contentType: FeatureValue.ScalarType = TextType
  override def makeList(config: BoundedListConfig): Resource[IO, BoundedList[Num]] =
    Resource.make(IO(new RedisNumBoundedList(redisClient, config)))(_ => IO.unit)

  override def makeValue(i: Int): Num = Num(i)
}
