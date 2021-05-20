package io.findify.featury.persistence.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.PeriodicCounterSuite
import io.findify.featury.model.Feature
import io.findify.featury.persistence.mem.MemPeriodicCounter

class MemPeriodicCounterTest extends PeriodicCounterSuite {
  override val feature: Feature.PeriodicCounter = MemPeriodicCounter(config, Scaffeine().build())

}
