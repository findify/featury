package io.findify.featury.persistence.memory

import cats.effect.IO
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import io.findify.featury.feature.ScalarFeature
import io.findify.featury.feature.ScalarFeature.{ScalarConfig, ScalarState}
import io.findify.featury.model.FeatureValue.{Num, NumScalarValue, Scalar, Text, TextScalarValue}
import io.findify.featury.model.{FeatureValue, Key}

trait MemScalarFeature[T <: Scalar] extends ScalarFeature[T] {
  def cache: Cache[Key, ScalarState[T]]

  override def readState(key: Key): IO[Option[ScalarState[T]]] = IO { cache.getIfPresent(key) }
  override def put(key: Key, value: T): IO[Unit]               = IO { cache.put(key, ScalarState(value)) }
}

object MemScalarFeature {
  case class MemTextScalarFeature(config: ScalarConfig, cache: Cache[Key, ScalarState[Text]])
      extends MemScalarFeature[Text] {
    override def computeValue(state: ScalarState[Text]): Option[FeatureValue.ScalarValue[Text]] = Some(
      TextScalarValue(state.value)
    )
  }
  case class MemNumScalarFeature(config: ScalarConfig, cache: Cache[Key, ScalarState[Num]])
      extends MemScalarFeature[Num] {
    override def computeValue(state: ScalarState[Num]): Option[FeatureValue.ScalarValue[Num]] = Some(
      NumScalarValue(state.value)
    )
  }
}
