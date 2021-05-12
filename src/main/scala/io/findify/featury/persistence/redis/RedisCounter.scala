package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.feature.Counter
import io.findify.featury.feature.Counter.{CounterConfig, CounterState}
import io.findify.featury.model.{BackendError, Key}
import redis.clients.jedis.Jedis

import scala.util.{Failure, Success, Try}

class RedisCounter(val config: CounterConfig, val redis: Jedis) extends Counter {
  import KeyCodec._
  override def increment(key: Key, value: Double): IO[Unit] = IO { redis.incrByFloat(key.toRedisKey("state"), value) }

  override def readState(key: Key): IO[Counter.CounterState] = for {
    bytes <- IO { Option(redis.get(key.toRedisKey("state"))) }
    value <- IO.fromEither(parseDouble(bytes))
  } yield {
    CounterState(value)
  }

  def parseDouble(bytes: Option[String]): Either[BackendError, Double] = bytes match {
    case None => Right(0.0)
    case Some(b) =>
      Try(java.lang.Double.parseDouble(b)) match {
        case Failure(exception) => Left(BackendError(exception.getMessage))
        case Success(value)     => Right(value)
      }
  }
}
