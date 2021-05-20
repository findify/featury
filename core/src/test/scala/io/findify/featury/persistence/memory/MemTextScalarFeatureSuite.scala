package io.findify.featury.persistence.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.ScalarFeatureSuite
import io.findify.featury.model.Feature
import io.findify.featury.model.FeatureValue.Text
import io.findify.featury.persistence.mem.MemScalarFeature.MemTextScalarFeature

class MemTextScalarFeatureSuite extends ScalarFeatureSuite[Text] {
  override def makeValue(i: Int): Text              = Text(i.toString)
  override def feature: Feature.ScalarFeature[Text] = MemTextScalarFeature(config, Scaffeine().build())
}
