package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.StatsEstimatorSuite
import io.findify.featury.model.{Feature, NumStatsValue}
import io.findify.featury.model.Feature.StatsEstimator
import io.findify.featury.model.Write.PutStatSample
import io.findify.featury.state.mem.MemStatsEstimator

class MemStatsEstimatorTest extends StatsEstimatorSuite with MemTest[PutStatSample, StatsEstimator] {
  override val feature: Feature.StatsEstimator = MemStatsEstimator(config, Scaffeine().build())
}
