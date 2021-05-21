package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.ScalarFeatureSuite
import io.findify.featury.model.{Feature, Key, SString, Timestamp, Write}
import io.findify.featury.model.Write.PutString
import io.findify.featury.state.mem.MemScalarFeature.MemTextScalarFeature

class MemStrScalarFeatureSuite extends ScalarFeatureSuite[SString] {
  override def makePut(key: Key, ts: Timestamp, i: Int): Write.Put[SString] = PutString(key, ts, SString(i.toString))
  override def feature: Feature.ScalarFeature[SString]                      = MemTextScalarFeature(config, Scaffeine().build())
}
