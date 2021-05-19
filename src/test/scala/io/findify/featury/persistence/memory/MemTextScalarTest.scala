package io.findify.featury.persistence.memory

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{ScalarFeature, ScalarFeatureSuite}
import io.findify.featury.model.FeatureValue.Text

import scala.util.Random

class MemTextScalarTest extends ScalarFeatureSuite[Text] {
  override def makeValue: Text = Text(Random.nextInt(1000000).toString)

  override def makeCounter(): Resource[IO, ScalarFeature[Text]] =
    Resource.make(MemPersistence.textScalar(config))(_ => IO.unit)
}
