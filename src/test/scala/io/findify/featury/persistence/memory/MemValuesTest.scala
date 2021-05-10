package io.findify.featury.persistence.memory

import cats.effect.{IO, Resource}
import io.findify.featury.feature.ValuesSuite
import io.findify.featury.persistence.ValueStore

class MemValuesTest extends ValuesSuite {
  override def makeValues(): Resource[IO, ValueStore] = Resource.make(IO(MemPersistence.values()))(_ => IO.unit)
}
