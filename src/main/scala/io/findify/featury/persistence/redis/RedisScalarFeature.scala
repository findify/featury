package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.feature.ScalarFeature
import io.findify.featury.feature.ScalarFeature.{ScalarConfig, ScalarState}
import io.findify.featury.model.FeatureValue.{Num, NumScalarValue, Scalar, Text, TextScalarValue}
import io.findify.featury.model.{FeatureValue, Key}
import redis.clients.jedis.Jedis

trait RedisScalarFeature[T <: Scalar] extends ScalarFeature[T] with RedisFeature {
  override val keySuffix = "s"
  import KeyCodec._
  def codec: Codec[T]

  override def put(key: Key, value: T): IO[Unit] = IO {
    redis.set(key.toRedisKey(keySuffix), codec.encode(value))
  }

  override def readState(key: Key): IO[Option[ScalarFeature.ScalarState[T]]] = for {
    value <- IO { Option(redis.get(key.toRedisKey(keySuffix))) }
    decoded <- IO.fromEither(value match {
      case Some(str) => codec.decode(str).map(Some.apply)
      case None      => Right(None)
    })
  } yield {
    decoded.map(ScalarState.apply)
  }
}

object RedisScalarFeature {
  case class RedisTextScalarFeature(config: ScalarConfig, redis: Jedis)(implicit val codec: Codec[Text])
      extends RedisScalarFeature[Text] {
    override def computeValue(state: ScalarState[Text]): Option[FeatureValue.ScalarValue[Text]] = Some(
      TextScalarValue(state.value)
    )
  }
  case class RedisNumScalarFeature(config: ScalarConfig, redis: Jedis)(implicit val codec: Codec[Num])
      extends RedisScalarFeature[Num] {
    override def computeValue(state: ScalarState[Num]): Option[FeatureValue.ScalarValue[Num]] = Some(
      NumScalarValue(state.value)
    )
  }
}
