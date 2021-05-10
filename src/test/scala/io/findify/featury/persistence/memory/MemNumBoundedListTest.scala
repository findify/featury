package io.findify.featury.persistence.memory

import cats.effect.{IO, Resource}
import io.findify.featury.feature.BoundedList.BoundedListConfig
import io.findify.featury.feature.{BoundedList, BoundedListSuite}
import io.findify.featury.model.FeatureValue
import io.findify.featury.model.FeatureValue.{Num, NumType, Text}

class MemNumBoundedListTest extends BoundedListSuite[Num] {
  override def contentType: FeatureValue.ScalarType = NumType
  override def makeList(config: BoundedListConfig): Resource[IO, BoundedList[Num]] =
    Resource.make(IO(MemPersistence.numBoundedList(config)))(_ => IO.unit)

  override def makeValue(i: Int): Num = Num(i)
}
