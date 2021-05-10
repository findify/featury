package io.findify.featury.persistence.memory

import io.findify.featury.model.StoreConfig
import io.findify.featury.persistence.BoundedListStoreSuite
import io.findify.featury.persistence.StateStore.TextListStore
import cats.effect.{IO, Resource}
import scala.concurrent.duration._

class BoundedListMemoryStoreTest extends BoundedListStoreSuite[TextListStore] {
  val c                          = StoreConfig(1.hour)
  override def makePersistence() = Resource.make(IO(MemoryStore(c).textList))(_ => IO.unit)
}
