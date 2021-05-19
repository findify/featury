package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.feature.PeriodicCounter
import io.findify.featury.feature.PeriodicCounter.{PeriodicCounterConfig, PeriodicCounterState}
import io.findify.featury.model.{BackendError, Key, Timestamp}
import redis.clients.jedis.Jedis

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

case class RedisPeriodicCounter(config: PeriodicCounterConfig, redis: Jedis) extends PeriodicCounter with RedisFeature {
  val keySuffix = "pc"
  import KeyCodec._
  override def increment(key: Key, ts: Timestamp, value: Long): IO[Unit] = for {
    _ <- IO { redis.hincrByFloat(key.toRedisKey(keySuffix), ts.toStartOfPeriod(config.period).ts.toString, value) }
  } yield {}

  override def readState(key: Key): IO[Option[PeriodicCounterState]] = for {
    responseOption <- IO { Option(redis.hgetAll(key.toRedisKey(keySuffix))).map(_.asScala.toList) }
    decoded <- responseOption match {
      case None      => IO.pure(None)
      case Some(Nil) => IO.pure(None)
      case Some(response) =>
        parseResponse(response).map(parsed => if (parsed.nonEmpty) Some(PeriodicCounterState(parsed)) else None)
    }
  } yield {
    decoded
  }

  @tailrec private def parseResponse(
      values: List[(String, String)],
      acc: List[(Timestamp, Long)] = Nil
  ): IO[Map[Timestamp, Long]] =
    values match {
      case Nil => IO(acc.toMap)
      case (key, value) :: tail =>
        (Try(java.lang.Long.parseLong(key)), Try(java.lang.Long.parseLong(value))) match {
          case (Success(ts), Success(inc)) => parseResponse(tail, (Timestamp(ts) -> inc) :: acc)
          case (Failure(ex), _)            => IO.raiseError(ex)
          case (_, Failure(ex))            => IO.raiseError(ex)
        }
    }
}
