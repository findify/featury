package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.ScalarFeatureSuite
import io.findify.featury.model.Write.Put
import io.findify.featury.model.{Feature, Key, SDouble, Timestamp, Write}
import io.findify.featury.state.mem.MemScalarFeature

class MemSDoubleScalarFeatureTest extends ScalarFeatureSuite {
  override def makePut(key: Key, ts: Timestamp, i: Int): Put = Put(key, ts, SDouble(i))

  override def feature: Feature.ScalarFeature = MemScalarFeature(config, Scaffeine().build())
}
