package io.findify.featury.persistence.memory

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{ScalarFeature, ScalarFeatureSuite}
import io.findify.featury.model.FeatureValue.{Num, Text}

import scala.util.Random

class MemNumScalarTest extends ScalarFeatureSuite[Num] {
  override def makeValue: Num = Num(Random.nextInt(1000000).toDouble)

  override def makeCounter(): Resource[IO, ScalarFeature[Num]] =
    Resource.make(MemPersistence.numScalar(config))(_ => IO.unit)
}
