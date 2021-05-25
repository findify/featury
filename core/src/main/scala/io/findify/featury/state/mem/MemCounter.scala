package io.findify.featury.state.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.Counter
import io.findify.featury.model.FeatureConfig.CounterConfig
import io.findify.featury.model.Write.Increment
import io.findify.featury.model.{CounterState, CounterValue, Key, SLong, Timestamp}

case class MemCounter(config: CounterConfig, cache: Cache[Key, Long]) extends Counter {
  override def put(action: Increment): Unit = {
    cache.getIfPresent(action.key) match {
      case Some(counter) => cache.put(action.key, counter + action.inc)
      case None          => cache.put(action.key, action.inc)
    }

  }
  override def computeValue(key: Key, ts: Timestamp): Option[CounterValue] = {
    cache.getIfPresent(key).map(c => CounterValue(key, ts, c))
  }

  override def readState(key: Key, ts: Timestamp): Option[CounterState] =
    cache.getIfPresent(key).map(CounterState(key, ts, _))

  override def writeState(state: CounterState): Unit = cache.put(state.key, state.value)
}
