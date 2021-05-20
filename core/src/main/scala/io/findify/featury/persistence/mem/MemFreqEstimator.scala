package io.findify.featury.persistence.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.FreqEstimator
import io.findify.featury.model.FeatureConfig.FreqEstimatorConfig
import io.findify.featury.model.FeatureValue.FrequencyValue
import io.findify.featury.model.{FeatureValue, Key, WriteRequest}

import scala.util.Random

case class MemFreqEstimator(config: FreqEstimatorConfig, cache: Cache[Key, Vector[String]]) extends FreqEstimator {
  override def putSampled(action: WriteRequest.PutFreqSample): Unit =
    cache.getIfPresent(action.key) match {
      case Some(pool) if pool.size < config.poolSize =>
        cache.put(action.key, action.value +: pool)
      case Some(pool) =>
        val index = Random.nextInt(config.poolSize)
        cache.put(action.key, pool.updated(index, action.value))
      case None =>
        cache.put(action.key, Vector(action.value))
    }

  override def computeValue(key: Key): Option[FeatureValue.FrequencyValue] = for {
    pool <- cache.getIfPresent(key) if pool.nonEmpty
  } yield {
    val sum = pool.size.toDouble
    val result = pool.groupBy(identity).map { case (key, values) =>
      key -> values.size / sum
    }
    FrequencyValue(result)
  }
}
