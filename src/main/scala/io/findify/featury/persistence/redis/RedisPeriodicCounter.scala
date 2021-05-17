package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.feature.PeriodicCounter
import io.findify.featury.feature.PeriodicCounter.{PeriodicCounterConfig, PeriodicCounterState}
import io.findify.featury.model.{BackendError, Key, Timestamp}
import redis.clients.jedis.Jedis

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class RedisPeriodicCounter(val config: PeriodicCounterConfig, redis: Jedis) extends PeriodicCounter {
  val SUFFIX    = "pc"
  val SUFFIX_TS = "pct"
  import KeyCodec._
  override def increment(key: Key, ts: Timestamp, value: Double): IO[Unit] = for {
    _ <- IO { redis.hincrByFloat(key.toRedisKey(SUFFIX), ts.toStartOfPeriod(config.period).ts.toString, value) }
    _ <- IO { redis.set(key.toRedisKey(SUFFIX_TS), ts.ts.toString) }
  } yield {}

  override def readState(key: Key): IO[PeriodicCounter.PeriodicCounterState] = for {
    responseOption <- IO { Option(redis.hgetAll(key.toRedisKey(SUFFIX))).map(_.asScala) }
    nowOption      <- IO { Option(redis.get(key.toRedisKey(SUFFIX_TS))) }
    now <- nowOption match {
      case Some(value) => IO.fromTry(Try(java.lang.Long.parseLong(value)))
      case None        => IO.raiseError(BackendError(s"cannot parse ts: $nowOption"))
    }
    decoded <- responseOption match {
      case None           => IO.pure(empty())
      case Some(response) => parseResponse(response.toList).map(x => PeriodicCounterState(Timestamp(now), x))
    }
  } yield {
    decoded
  }

  @tailrec private def parseResponse(
      values: List[(String, String)],
      acc: List[(Timestamp, Double)] = Nil
  ): IO[Map[Timestamp, Double]] =
    values match {
      case Nil => IO(acc.toMap)
      case (key, value) :: tail =>
        (Try(java.lang.Long.parseLong(key)), Try(java.lang.Double.parseDouble(value))) match {
          case (Success(ts), Success(inc)) => parseResponse(tail, (Timestamp(ts) -> inc) :: acc)
          case (Failure(ex), _)            => IO.raiseError(ex)
          case (_, Failure(ex))            => IO.raiseError(ex)
        }
    }
}
