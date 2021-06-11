package io.findify.featury.state.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.FreqEstimator
import io.findify.featury.model.FeatureConfig.FreqEstimatorConfig
import io.findify.featury.model.Write.PutFreqSample
import io.findify.featury.model.{FeatureValue, FrequencyState, FrequencyValue, Key, Timestamp, WriteRequest}

import scala.util.Random

case class MemFreqEstimator(config: FreqEstimatorConfig, cache: Cache[Key, List[String]]) extends FreqEstimator {
  override def putSampled(action: PutFreqSample): Unit =
    cache.getIfPresent(action.key) match {
      case Some(pool) if pool.size < config.poolSize =>
        cache.put(action.key, action.value +: pool)
      case Some(pool) =>
        val index = Random.nextInt(config.poolSize)
        cache.put(action.key, (action.value +: pool).take(config.poolSize))
      case None =>
        cache.put(action.key, List(action.value))
    }

  override def computeValue(key: Key, ts: Timestamp): Option[FrequencyValue] = for {
    pool <- cache.getIfPresent(key) if pool.nonEmpty
  } yield {
    val sum = pool.size.toDouble
    val result = pool.groupBy(identity).map { case (key, values) =>
      key -> values.size / sum
    }
    FrequencyValue(key, ts, result)
  }

  override def readState(key: Key, ts: Timestamp): Option[FrequencyState] =
    cache.getIfPresent(key).map(FrequencyState(key, ts, _))

  override def writeState(state: FrequencyState): Unit = cache.put(state.key, state.values)
}
