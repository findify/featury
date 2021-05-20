package io.findify.featury.persistence.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.BoundedListSuite
import io.findify.featury.model.Feature
import io.findify.featury.model.FeatureValue.Text
import io.findify.featury.persistence.mem.MemBoundedList.MemTextBoundedList

class MemTextBoundedListTest extends BoundedListSuite[Text] {
  override def makeValue(i: Int): Text            = Text(i.toString)
  override val feature: Feature.BoundedList[Text] = MemTextBoundedList(config, Scaffeine().build())
}
