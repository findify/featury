package io.findify.featury.persistence.memory

import cats.effect.IO
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import io.findify.featury.feature.Counter
import io.findify.featury.feature.Counter.{CounterConfig, CounterState}
import io.findify.featury.model.{FeatureValue, Key}

class MemCounter(val config: CounterConfig, counterCache: Cache[Key, CounterState]) extends Counter {
  override def readState(key: Key): IO[Counter.CounterState] = IO { counterCache.getIfPresent(key).getOrElse(empty()) }

  override def increment(key: Key, value: Double): IO[Unit] = IO {
    counterCache.getIfPresent(key) match {
      case Some(existing) => counterCache.put(key, existing.increment(value))
      case None           => counterCache.put(key, CounterState(value))
    }
  }

}