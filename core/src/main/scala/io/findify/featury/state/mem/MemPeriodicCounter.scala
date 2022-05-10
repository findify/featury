package io.findify.featury.state.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.PeriodicCounter
import io.findify.featury.model.FeatureConfig.PeriodicCounterConfig
import io.findify.featury.model.PeriodicCounterState.TimeCounter
import io.findify.featury.model.Write.PeriodicIncrement
import io.findify.featury.model.{FeatureValue, Key, PeriodicCounterState, PeriodicCounterValue, Timestamp}

case class MemPeriodicCounter(config: PeriodicCounterConfig, cache: Cache[Key, Map[Timestamp, Long]])
    extends PeriodicCounter {
  override def put(action: PeriodicIncrement): Unit = {
    cache.getIfPresent(action.key) match {
      case None =>
        cache.put(action.key, Map(action.ts.toStartOfPeriod(config.period) -> action.inc))
      case Some(counters) =>
        val timeKey = action.ts.toStartOfPeriod(config.period)
        val incremented = counters.get(timeKey) match {
          case Some(count) => count + action.inc
          case None        => action.inc.toLong
        }
        cache.put(action.key, counters + (timeKey -> incremented))
    }
  }

  override def computeValue(key: Key, ts: Timestamp): Option[PeriodicCounterValue] =
    cache.getIfPresent(key).map(values => PeriodicCounterValue(key, ts, fromMap(values)))

  override def readState(key: Key, ts: Timestamp): Option[PeriodicCounterState] =
    cache
      .getIfPresent(key)
      .map(counters => PeriodicCounterState(key, ts, counters.map(c => TimeCounter(c._1, c._2)).toList))

  override def writeState(state: PeriodicCounterState): Unit =
    cache.put(state.key, state.values.map(kv => kv.ts -> kv.count).toMap)
}
