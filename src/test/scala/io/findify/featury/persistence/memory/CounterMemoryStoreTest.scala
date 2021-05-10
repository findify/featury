package io.findify.featury.persistence.memory

import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.model.StoreConfig
import io.findify.featury.persistence.CounterStoreSuite
import io.findify.featury.persistence.memory.MemoryStore.CounterMemoryStore
import cats.effect.{IO, Resource}

import scala.concurrent.duration._

class CounterMemoryStoreTest extends CounterStoreSuite[CounterMemoryStore] {
  val c                          = StoreConfig(1.minute)
  override def makePersistence() = Resource.make(IO(CounterMemoryStore(c)))(_ => IO.unit)
}
