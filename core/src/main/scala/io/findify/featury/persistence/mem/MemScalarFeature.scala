package io.findify.featury.persistence.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.ScalarFeature
import io.findify.featury.model.FeatureConfig.ScalarConfig
import io.findify.featury.model.FeatureValue.{Num, Scalar, ScalarValue, Text}
import io.findify.featury.model.{FeatureValue, Key, WriteRequest}

sealed trait MemScalarFeature[T <: Scalar] extends ScalarFeature[T] {
  def cache: Cache[Key, T]
  override def put(action: WriteRequest.Put[T]): Unit = cache.put(action.key, action.value)

  override def computeValue(key: Key): Option[FeatureValue.ScalarValue[T]] =
    cache.getIfPresent(key).map(value => new ScalarValue(value))
}

object MemScalarFeature {
  case class MemTextScalarFeature(config: ScalarConfig, cache: Cache[Key, Text]) extends MemScalarFeature[Text]
  case class MemNumScalarFeature(config: ScalarConfig, cache: Cache[Key, Num])   extends MemScalarFeature[Num]
}
