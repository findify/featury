package io.findify.featury.state.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.PeriodicCounter
import io.findify.featury.model.FeatureConfig.PeriodicCounterConfig
import io.findify.featury.model.{FeatureValue, Key, Timestamp, WriteRequest}

case class MemPeriodicCounter(config: PeriodicCounterConfig, cache: Cache[Key, Map[Timestamp, Long]])
    extends PeriodicCounter {
  override def put(action: WriteRequest.PeriodicIncrement): Unit = {
    cache.getIfPresent(action.key) match {
      case None =>
        cache.put(action.key, Map(action.ts.toStartOfPeriod(config.period) -> action.inc))
      case Some(counters) =>
        val timeKey = action.ts.toStartOfPeriod(config.period)
        val incremented = counters.get(timeKey) match {
          case Some(count) => count + action.inc
          case None        => action.inc
        }
        cache.put(action.key, counters + (timeKey -> incremented))
    }
  }

  override def computeValue(key: Key): Option[FeatureValue.PeriodicCounterValue] =
    cache.getIfPresent(key).map(fromMap)
}
