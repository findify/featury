package io.findify.featury.values

import cats.effect.{IO, Resource}
import io.findify.featury.StoreTestSuite

class MemoryStoreTest extends StoreTestSuite {
  override def storeResource: Resource[IO, FeatureStore] =
    Resource.make(IO(new MemoryStore()))(_ => IO.unit)
}
