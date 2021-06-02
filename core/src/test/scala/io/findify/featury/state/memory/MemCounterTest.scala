package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.CounterSuite
import io.findify.featury.model.Feature.Counter
import io.findify.featury.model.Write.Increment
import io.findify.featury.model.{CounterValue, Feature, FeatureValue, Key, Write}
import io.findify.featury.state.mem.MemCounter

class MemCounterTest extends CounterSuite with MemTest[Increment, Counter] {
  override val feature = MemCounter(config, Scaffeine().build[Key, Long]())
}
