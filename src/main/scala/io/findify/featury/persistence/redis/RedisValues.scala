package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.model.{BackendError, FeatureValue, Key, ReadResponse}
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.ReadResponse.ItemFeatures
import io.findify.featury.persistence.ValueStore
import redis.clients.jedis.Jedis

import scala.collection.JavaConverters._

class RedisValues(val redis: Jedis)(implicit c: Codec[FeatureValue]) extends ValueStore {
  import KeyCodec._
  override def readBatch(keys: List[Key]): IO[List[ReadResponse.ItemFeatures]] = {
    for {
      values  <- IO { redis.mget(keys.map(_.toRedisKey("val")): _*) }
      decoded <- parseBatch(keys, values.asScala.toList)
    } yield {
      decoded
    }
  }

  override def write(key: Key, value: FeatureValue): IO[Unit] = {
    val encoded = c.encode(value)
    for {
      _ <- IO { redis.set(key.toRedisKey("val"), encoded) }
    } yield {}
  }

  def parseBatch(keys: List[Key], values: List[String]): IO[List[ReadResponse.ItemFeatures]] = {
    val result =
      values.map(Option.apply).zip(keys).reverse.foldLeft[Either[Throwable, List[ItemFeatures]]](Right(Nil)) {
        case (Right(acc), (None, key)) => Right(ItemFeatures(key, None) :: acc)
        case (Right(acc), (Some(next), key)) =>
          c.decode(next) match {
            case Left(err)    => Left(err)
            case Right(value) => Right(ItemFeatures(key, Some(value)) :: acc)
          }
        case (Left(err), _) => Left(err)
      }
    IO.fromEither(result)
  }
}
