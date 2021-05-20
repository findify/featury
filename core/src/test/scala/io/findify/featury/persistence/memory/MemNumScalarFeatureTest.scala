package io.findify.featury.persistence.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.ScalarFeatureSuite
import io.findify.featury.model.Feature
import io.findify.featury.model.FeatureValue.Num
import io.findify.featury.persistence.mem.MemScalarFeature.MemNumScalarFeature

class MemNumScalarFeatureTest extends ScalarFeatureSuite[Num] {
  override def makeValue(i: Int): Num = Num(i)

  override def feature: Feature.ScalarFeature[Num] = MemNumScalarFeature(config, Scaffeine().build())
}
