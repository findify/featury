package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.FreqEstimatorSuite
import io.findify.featury.model.Feature.FreqEstimator
import io.findify.featury.model.{Feature, FrequencyValue}
import io.findify.featury.model.Write.PutFreqSample
import io.findify.featury.state.mem.MemFreqEstimator

class MemFreqEstimatorTest extends FreqEstimatorSuite with MemTest[PutFreqSample, FrequencyValue, FreqEstimator] {
  override val feature: Feature.FreqEstimator = MemFreqEstimator(config, Scaffeine().build())
}
