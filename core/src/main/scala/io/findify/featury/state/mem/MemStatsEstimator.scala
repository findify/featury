package io.findify.featury.state.mem

import com.github.blemale.scaffeine.Cache
import com.google.common.math.Quantiles
import io.findify.featury.model.Feature.StatsEstimator
import io.findify.featury.model.FeatureConfig.StatsEstimatorConfig
import io.findify.featury.model.Write.PutStatSample
import io.findify.featury.model.{FeatureValue, Key, NumStatsValue, StatsState, Timestamp}

import scala.collection.JavaConverters._
import scala.util.Random

case class MemStatsEstimator(config: StatsEstimatorConfig, cache: Cache[Key, List[Double]]) extends StatsEstimator {
  override def putSampled(action: PutStatSample): Unit = {
    cache.getIfPresent(action.key) match {
      case None =>
        cache.put(action.key, List(action.value))
      case Some(pool) if pool.size < config.poolSize =>
        cache.put(action.key, action.value +: pool)
      case Some(pool) =>
        val index = Random.nextInt(config.poolSize)
        cache.put(action.key, (action.value +: pool).take(config.poolSize))
    }
  }

  override def computeValue(key: Key, ts: Timestamp): Option[NumStatsValue] = {
    for {
      pool <- cache.getIfPresent(key) if pool.nonEmpty
    } yield {
      fromPool(key, ts, pool)
    }
  }

  override def readState(key: Key, ts: Timestamp): Option[StatsState] =
    cache.getIfPresent(key).map(StatsState(key, ts, _))

  override def writeState(state: StatsState): Unit = cache.put(state.key, state.values)
}
