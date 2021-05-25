package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.ScalarFeatureSuite
import io.findify.featury.model.Write.Put
import io.findify.featury.model.{Feature, Key, SString, Timestamp, Write}
import io.findify.featury.state.mem.MemScalarFeature

class MemStrScalarFeatureSuite extends ScalarFeatureSuite {
  override def makePut(key: Key, ts: Timestamp, i: Int): Write.Put = Put(key, ts, SString(i.toString))
  override def feature: Feature.ScalarFeature                      = MemScalarFeature(config, Scaffeine().build())
}
