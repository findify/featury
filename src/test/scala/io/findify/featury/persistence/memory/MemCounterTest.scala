package io.findify.featury.persistence.memory

import cats.effect.{IO, Resource}
import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.feature.CounterSuite
import io.findify.featury.model.Key.FeatureName

class MemCounterTest extends CounterSuite {
  val config                 = CounterConfig(FeatureName("counter"))
  override def makeCounter() = Resource.make(IO(MemPersistence.counter(config)))(_ => IO.unit)
}
