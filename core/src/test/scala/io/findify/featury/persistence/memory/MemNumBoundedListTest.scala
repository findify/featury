package io.findify.featury.persistence.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.BoundedListSuite
import io.findify.featury.model.Feature
import io.findify.featury.model.FeatureValue.Num
import io.findify.featury.persistence.mem.MemBoundedList.MemNumBoundedList

class MemNumBoundedListTest extends BoundedListSuite[Num] {
  override def makeValue(i: Int): Num            = Num(i)
  override val feature: Feature.BoundedList[Num] = MemNumBoundedList(config, Scaffeine().build())
}
