package io.findify.featury.persistence.memory

import cats.effect.IO
import com.github.blemale.scaffeine.Cache
import io.findify.featury.feature.StatsEstimator
import io.findify.featury.feature.StatsEstimator.{StatsEstimatorConfig, StatsEstimatorState}
import io.findify.featury.model.{FeatureValue, Key}

import scala.util.Random

class MemStatsEstimator(val config: StatsEstimatorConfig, cache: Cache[Key, StatsEstimatorState])
    extends StatsEstimator {

  override def putReal(key: Key, value: Double): IO[Unit] = IO {
    val state = cache.getIfPresent(key).getOrElse(empty())
    val updated = if (state.samples.length < config.poolSize) {
      StatsEstimatorState(value +: state.samples)
    } else {
      val position = Random.nextInt(config.poolSize)
      StatsEstimatorState(state.samples.updated(position, value))
    }
    cache.put(key, updated)
  }

  override def readState(key: Key): IO[StatsEstimatorState] = IO {
    cache.getIfPresent(key).getOrElse(empty())
  }
}
