package io.findify.featury.persistence.memory

import cats.effect.{IO, Resource}
import io.findify.featury.feature.BoundedList.BoundedListConfig
import io.findify.featury.feature.{BoundedList, BoundedListSuite}
import io.findify.featury.model.FeatureValue
import io.findify.featury.model.FeatureValue.{Text, TextType}

class MemTextBoundedListTest extends BoundedListSuite[Text] {
  override def contentType: FeatureValue.ScalarType = TextType
  override def makeList(config: BoundedListConfig): Resource[IO, BoundedList[Text]] =
    Resource.make(IO(MemPersistence.textBoundedList(config)))(_ => IO.unit)

  override def makeValue(i: Int): Text = Text(i.toString)
}
