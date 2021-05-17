package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.model.{BackendError, FeatureValue, Key, ReadResponse}
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.ReadResponse.ItemFeatures
import io.findify.featury.persistence.ValueStore
import io.findify.featury.persistence.ValueStore.{BatchResult, KeyBatch, KeyFeatures}
import redis.clients.jedis.Jedis

import scala.collection.JavaConverters._

class RedisValues(val redis: Jedis)(implicit c: Codec[FeatureValue]) extends ValueStore {
  import KeyCodec._
  override def readBatch(batch: KeyBatch): IO[BatchResult] = {
    for {
      keys    <- IO { batch.asKeys }
      values  <- IO { redis.mget(keys.map(_.toRedisKey("val")): _*) }
      decoded <- parseBatch(keys, values.asScala.toList)
    } yield {
      BatchResult(batch.ns, batch.group, decoded)
    }
  }

  override def write(key: Key, value: FeatureValue): IO[Unit] = {
    val encoded = c.encode(value)
    for {
      _ <- IO { redis.set(key.toRedisKey("val"), encoded) }
    } yield {}
  }

  def parseBatch(keys: List[Key], values: List[String]): IO[List[KeyFeatures]] = {
    val result =
      values
        .map(Option.apply)
        .zip(keys)
        .foldLeft[Either[Throwable, List[ItemFeatures]]](Right(Nil)) {
          case (Right(acc), (None, _)) => Right(acc)
          case (Right(acc), (Some(next), key)) =>
            c.decode(next) match {
              case Left(err)    => Left(err)
              case Right(value) => Right(ItemFeatures(key, value) :: acc)
            }
          case (Left(err), _) => Left(err)
        }
        .map(
          _.groupBy(_.key.id)
            .map { case (id, values) =>
              KeyFeatures(
                id = id,
                features = values
                  .groupBy(_.key.featureName)
                  .flatMap {
                    case (name, head :: _) => Some(name -> head.value)
                    case (_, Nil)          => None
                  }
              )
            }
            .toList
            .reverse
        )
    IO.fromEither(result)
  }
}
