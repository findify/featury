package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.BoundedListSuite
import io.findify.featury.model.{Feature, SString}
import io.findify.featury.state.mem.MemBoundedList

class MemStrBoundedListTest extends BoundedListSuite {
  override def makeValue(i: Int)            = SString(i.toString)
  override val feature: Feature.BoundedList = MemBoundedList(config, Scaffeine().build())
}
