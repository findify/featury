package io.findify.featury.persistence.redis

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{ScalarFeature, ScalarFeatureSuite}
import io.findify.featury.model.FeatureValue.{Num, Text}
import io.findify.featury.persistence.redis.RedisScalarFeature.RedisNumScalarFeature

import scala.util.Random

class RedisNumScalarFeature extends ScalarFeatureSuite[Num] with RedisClient {
  override def makeValue: Num = Num(Random.nextInt(100000).toDouble)
  override def makeCounter(): Resource[IO, ScalarFeature[Num]] =
    Resource.make(IO(RedisNumScalarFeature(config, redisClient)))(_ => IO.unit)
}
