package io.findify.featury.values

import cats.effect.{IO, Resource}
import io.findify.featury.StoreTestSuite

class MemoryStoreTest extends StoreTestSuite[MemoryStore] {
  override def storeResource = Resource.make(IO(MemoryStore()))(_ => IO.unit)
}
