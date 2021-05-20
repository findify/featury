package io.findify.featury.persistence.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.FreqEstimatorSuite
import io.findify.featury.model.Feature
import io.findify.featury.persistence.mem.MemFreqEstimator

class MemFreqEstimatorTest extends FreqEstimatorSuite {
  override def feature: Feature.FreqEstimator = MemFreqEstimator(config, Scaffeine().build())
}
