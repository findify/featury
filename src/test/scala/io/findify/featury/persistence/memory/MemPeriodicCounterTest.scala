package io.findify.featury.persistence.memory

import cats.effect.{IO, Resource}
import io.findify.featury.feature.PeriodicCounter.{PeriodRange, PeriodicCounterConfig}
import io.findify.featury.feature.{PeriodicCounter, PeriodicCounterSuite}

import scala.concurrent.duration._

class MemPeriodicCounterTest extends PeriodicCounterSuite {
  override def makeCounter() = Resource.make(MemPersistence.periodicCounter(config))(_ => IO.unit)
}
