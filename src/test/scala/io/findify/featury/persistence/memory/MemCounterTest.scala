package io.findify.featury.persistence.memory

import cats.effect.{IO, Resource}
import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.feature.CounterSuite
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}

class MemCounterTest extends CounterSuite {
  override def makeCounter() = Resource.make(MemPersistence.counter(config))(_ => IO.unit)
}
