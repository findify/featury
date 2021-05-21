package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.BoundedListSuite
import io.findify.featury.model.{Feature, SString}
import io.findify.featury.state.mem.MemBoundedList.MemTextBoundedList

class MemStrBoundedListTest extends BoundedListSuite[SString] {
  override def makeValue(i: Int): SString            = SString(i.toString)
  override val feature: Feature.BoundedList[SString] = MemTextBoundedList(config, Scaffeine().build())
}
