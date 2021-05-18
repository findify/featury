package io.findify.featury.persistence.memory

import cats.effect.IO
import com.github.blemale.scaffeine.Cache
import io.findify.featury.feature.PeriodicCounter
import io.findify.featury.feature.PeriodicCounter.{PeriodicCounterConfig, PeriodicCounterState}
import io.findify.featury.model.{Key, Timestamp}

class MemPeriodicCounter(val config: PeriodicCounterConfig, cache: Cache[Key, PeriodicCounterState])
    extends PeriodicCounter {
  override def increment(key: Key, ts: Timestamp, value: Long): IO[Unit] = IO {
    val periodStart = ts.toStartOfPeriod(config.period)
    val prev        = cache.getIfPresent(key).getOrElse(empty())
    val updated = prev.periods.get(periodStart) match {
      case Some(oldCounter) => prev.periods + (periodStart -> (value + oldCounter))
      case None             => prev.periods + (periodStart -> (value))
    }
    cache.put(key, PeriodicCounterState(updated))
  }

  override def readState(key: Key): IO[PeriodicCounterState] = IO {
    cache.getIfPresent(key).getOrElse(empty())
  }
}
