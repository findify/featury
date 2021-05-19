package io.findify.featury.persistence.redis

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{ScalarFeature, ScalarFeatureSuite}
import io.findify.featury.model.FeatureValue.Text
import io.findify.featury.persistence.redis.RedisScalarFeature.RedisTextScalarFeature

import scala.util.Random

class RedisTextScalarTest extends ScalarFeatureSuite[Text] with RedisClient {
  override def makeValue: Text = Text(Random.nextInt(100000).toString)
  override def makeCounter(): Resource[IO, ScalarFeature[Text]] =
    Resource.make(IO(RedisTextScalarFeature(config, redisClient)))(_ => IO.unit)
}
