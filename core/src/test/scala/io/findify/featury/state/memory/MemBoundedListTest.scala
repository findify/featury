package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.BoundedListSuite
import io.findify.featury.model.Feature.BoundedList
import io.findify.featury.model.Write.Append
import io.findify.featury.model.{BoundedListValue, Feature, SDouble}
import io.findify.featury.state.mem.MemBoundedList

class MemBoundedListTest extends BoundedListSuite with MemTest[Append, BoundedList] {
  override val feature: Feature.BoundedList = MemBoundedList(config, Scaffeine().build())
}
