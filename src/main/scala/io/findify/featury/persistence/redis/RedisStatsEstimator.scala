package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.feature.StatsEstimator
import io.findify.featury.feature.StatsEstimator.{StatsEstimatorConfig, StatsEstimatorState}
import io.findify.featury.model.Key
import redis.clients.jedis.Jedis

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class RedisStatsEstimator(val config: StatsEstimatorConfig, redis: Jedis) extends StatsEstimator {
  import KeyCodec._
  override def putReal(key: Key, value: Double): IO[Unit] = {
    val multi = redis.multi()
    multi.lpush(key.toRedisKey("stats"), value.toString)
    multi.ltrim(key.toRedisKey("stats"), 0, config.poolSize)
    IO { multi.exec() }
  }

  override def readState(key: Key): IO[StatsEstimator.StatsEstimatorState] = for {
    response <- IO { redis.lrange(key.toRedisKey("stats"), 0, -1) }
    decoded  <- parseRecursive(response.asScala.toList)
  } yield {
    StatsEstimatorState(decoded.toVector)
  }

  def parseRecursive(strings: List[String], doubles: List[Double] = Nil): IO[List[Double]] = strings match {
    case Nil => IO.pure(doubles)
    case head :: tail =>
      Try(head.toDouble) match {
        case Failure(exception) => IO.raiseError(exception)
        case Success(value)     => parseRecursive(tail, value :: doubles)
      }
  }
}
