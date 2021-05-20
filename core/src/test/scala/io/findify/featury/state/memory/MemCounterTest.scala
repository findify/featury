package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.CounterSuite
import io.findify.featury.model.{Feature, Key}
import io.findify.featury.state.mem.MemCounter

class MemCounterTest extends CounterSuite {
  override val feature: Feature.Counter = MemCounter(config, Scaffeine().build[Key, Long]())
}
