package io.findify.featury.state.memory

import com.github.blemale.scaffeine.Scaffeine
import io.findify.featury.features.MapFeatureSuite
import io.findify.featury.model.Feature.MapFeature
import io.findify.featury.model.{MapState, MapValue}
import io.findify.featury.model.Write.PutTuple
import io.findify.featury.state.mem.MemMapFeature

class MemMapFeatureTest extends MapFeatureSuite with MemTest[PutTuple, MapFeature] {
  override val feature = MemMapFeature(config, Scaffeine().build())
}
