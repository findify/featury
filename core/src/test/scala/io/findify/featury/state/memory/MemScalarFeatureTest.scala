package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.ScalarFeatureSuite
import io.findify.featury.model.Feature.ScalarFeature
import io.findify.featury.model.Write.Put
import io.findify.featury.model.{Feature, Key, SDouble, ScalarValue, Timestamp, Write}
import io.findify.featury.state.mem.MemScalarFeature

class MemScalarFeatureTest extends ScalarFeatureSuite with MemTest[Put, ScalarValue, ScalarFeature] {
  override val feature: Feature.ScalarFeature = MemScalarFeature(config, Scaffeine().build())
}
