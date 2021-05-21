package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.ScalarFeatureSuite
import io.findify.featury.model.{Feature, Key, SDouble, Timestamp, Write}
import io.findify.featury.model.Write.PutDouble
import io.findify.featury.state.mem.MemScalarFeature.MemNumScalarFeature

class MemSDoubleScalarFeatureTest extends ScalarFeatureSuite[SDouble] {
  override def makePut(key: Key, ts: Timestamp, i: Int): Write.Put[SDouble] = PutDouble(key, ts, SDouble(i))

  override def feature: Feature.ScalarFeature[SDouble] = MemNumScalarFeature(config, Scaffeine().build())
}
