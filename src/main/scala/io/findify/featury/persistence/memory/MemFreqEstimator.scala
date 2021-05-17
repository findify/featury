package io.findify.featury.persistence.memory

import cats.effect.IO
import com.github.blemale.scaffeine.Cache
import io.findify.featury.feature.FreqEstimator
import io.findify.featury.feature.FreqEstimator.{FreqEstimatorConfig, FreqEstimatorState}
import io.findify.featury.model.Key
import redis.clients.jedis.Jedis

import scala.util.Random

class MemFreqEstimator(val config: FreqEstimatorConfig, cache: Cache[Key, FreqEstimatorState]) extends FreqEstimator {
  override def putReal(key: Key, value: String): IO[Unit] = IO {
    val current = cache.getIfPresent(key).getOrElse(empty())
    val updated = if (current.samples.size < config.poolSize) {
      FreqEstimatorState(value +: current.samples)
    } else {
      val index = Random.nextInt(current.samples.size)
      FreqEstimatorState(current.samples.updated(index, value))
    }
    cache.put(key, updated)
  }

  override def readState(key: Key): IO[FreqEstimator.FreqEstimatorState] = IO {
    cache.getIfPresent(key).getOrElse(empty())
  }
}
