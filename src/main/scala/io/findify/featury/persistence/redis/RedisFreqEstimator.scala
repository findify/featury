package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.feature.FreqEstimator
import io.findify.featury.feature.FreqEstimator.{FreqEstimatorConfig, FreqEstimatorState}
import io.findify.featury.model.Key
import redis.clients.jedis.Jedis

import scala.collection.JavaConverters._

case class RedisFreqEstimator(config: FreqEstimatorConfig, redis: Jedis) extends FreqEstimator with RedisFeature {
  val keySuffix = "f"
  import KeyCodec._
  override def putReal(key: Key, value: String): IO[Unit] = {
    val multi = redis.multi()
    multi.lpush(key.toRedisKey(keySuffix), value)
    multi.ltrim(key.toRedisKey(keySuffix), 0, config.poolSize)
    IO { multi.exec() }
  }

  override def readState(key: Key): IO[Option[FreqEstimatorState]] = for {
    response <- IO { redis.lrange(key.toRedisKey(keySuffix), 0, -1) }
  } yield {
    if (response.isEmpty) None else Some(FreqEstimatorState(response.asScala.toVector))
  }
}
