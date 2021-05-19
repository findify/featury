package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.feature.Counter
import io.findify.featury.feature.Counter.{CounterConfig, CounterState}
import io.findify.featury.model.{BackendError, Key}
import redis.clients.jedis.Jedis

import scala.util.{Failure, Success, Try}

case class RedisCounter(config: CounterConfig, redis: Jedis) extends Counter with RedisFeature {
  override val keySuffix = "c"
  import KeyCodec._

  override def increment(key: Key, value: Long): IO[Unit] = IO { redis.incrByFloat(key.toRedisKey(keySuffix), value) }

  override def readState(key: Key): IO[Option[CounterState]] = for {
    bytes <- IO { Option(redis.get(key.toRedisKey(keySuffix))) }
    value <- IO.fromEither(parseLong(bytes))
  } yield {
    if (value != 0) Some(CounterState(value)) else None
  }

  def parseLong(bytes: Option[String]): Either[BackendError, Long] = bytes match {
    case None => Right(0)
    case Some(b) =>
      Try(java.lang.Long.parseLong(b)) match {
        case Failure(exception) => Left(BackendError(exception.getMessage))
        case Success(value)     => Right(value)
      }
  }
}
