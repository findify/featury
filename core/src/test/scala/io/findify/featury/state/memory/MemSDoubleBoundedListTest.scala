package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.BoundedListSuite
import io.findify.featury.model.{Feature, SDouble}
import io.findify.featury.state.mem.MemBoundedList.MemNumBoundedList

class MemSDoubleBoundedListTest extends BoundedListSuite[SDouble] {
  override def makeValue(i: Int): SDouble            = SDouble(i)
  override val feature: Feature.BoundedList[SDouble] = MemNumBoundedList(config, Scaffeine().build())
}
