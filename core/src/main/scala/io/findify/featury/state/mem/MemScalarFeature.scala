package io.findify.featury.state.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.ScalarFeature
import io.findify.featury.model.FeatureConfig.ScalarConfig
import io.findify.featury.model.Write.Put
import io.findify.featury.model._

sealed trait MemScalarFeature[T <: Scalar] extends ScalarFeature[T] {
  def cache: Cache[Key, T]
  override def put(action: Put[T]): Unit = cache.put(action.key, action.value)

  override def computeValue(key: Key, ts: Timestamp): Option[ScalarValue[T]] =
    cache.getIfPresent(key).map(makeValue(key, ts, _))
}

object MemScalarFeature {
  case class MemTextScalarFeature(config: ScalarConfig, cache: Cache[Key, SString]) extends MemScalarFeature[SString] {
    override def makeValue(key: Key, ts: Timestamp, value: SString): ScalarValue[SString] =
      StringScalarValue(key, ts, value)
  }
  case class MemNumScalarFeature(config: ScalarConfig, cache: Cache[Key, SDouble]) extends MemScalarFeature[SDouble] {
    override def makeValue(key: Key, ts: Timestamp, value: SDouble): ScalarValue[SDouble] =
      DoubleScalarValue(key, ts, value)
  }
}
