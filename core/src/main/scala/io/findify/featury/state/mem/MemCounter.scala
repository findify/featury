package io.findify.featury.state.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.Counter
import io.findify.featury.model.FeatureConfig.CounterConfig
import io.findify.featury.model.Write.Increment
import io.findify.featury.model.{FeatureValue, Key, LongScalarValue, SLong}

case class MemCounter(config: CounterConfig, cache: Cache[Key, Long]) extends Counter {
  override def put(action: Increment): Unit = {
    cache.getIfPresent(action.key) match {
      case Some(counter) => cache.put(action.key, counter + action.inc)
      case None          => cache.put(action.key, action.inc)
    }

  }
  override def computeValue(key: Key): Option[LongScalarValue] = {
    cache.getIfPresent(key).map(c => LongScalarValue(SLong(c)))
  }
}
