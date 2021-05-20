package io.findify.featury.persistence.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.StatsEstimatorSuite
import io.findify.featury.model.Feature
import io.findify.featury.persistence.mem.MemStatsEstimator

class MemStatsEstimatorTest extends StatsEstimatorSuite {
  override def feature: Feature.StatsEstimator = MemStatsEstimator(config, Scaffeine().build())
}
