package io.findify.featury.state.mem

import com.github.blemale.scaffeine.Cache
import io.findify.featury.model.Feature.ScalarFeature
import io.findify.featury.model.FeatureConfig.ScalarConfig
import io.findify.featury.model.Write.Put
import io.findify.featury.model._

case class MemScalarFeature(config: ScalarConfig, cache: Cache[Key, Scalar]) extends ScalarFeature {
  override def put(action: Put): Unit = cache.put(action.key, action.value)

  override def computeValue(key: Key, ts: Timestamp): Option[ScalarValue] =
    cache.getIfPresent(key).map(ScalarValue(key, ts, _))

  override def readState(key: Key, ts: Timestamp): Option[ScalarState] =
    cache.getIfPresent(key).map(ScalarState(key, ts, _))

  override def writeState(state: ScalarState): Unit = cache.put(state.key, state.value)
}
