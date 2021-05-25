package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.PeriodicCounterSuite
import io.findify.featury.model.Feature.PeriodicCounter
import io.findify.featury.model.{Feature, PeriodicCounterState, PeriodicCounterValue}
import io.findify.featury.model.Write.PeriodicIncrement
import io.findify.featury.state.mem.MemPeriodicCounter

class MemPeriodicCounterTest
    extends PeriodicCounterSuite
    with MemTest[PeriodicIncrement, PeriodicCounterValue, PeriodicCounter] {
  override val feature: Feature.PeriodicCounter = MemPeriodicCounter(config, Scaffeine().build())
}
