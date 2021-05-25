package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.BoundedListSuite
import io.findify.featury.model.{Feature, SDouble}
import io.findify.featury.state.mem.MemBoundedList

class MemSDoubleBoundedListTest extends BoundedListSuite {
  override def makeValue(i: Int)            = SDouble(i)
  override val feature: Feature.BoundedList = MemBoundedList(config, Scaffeine().build())
}
