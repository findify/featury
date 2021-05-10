package io.findify.featury.model

import io.findify.featury.model.ReadResponse.ItemFeatures

case class ReadResponse(tenant: Int, items: List[ItemFeatures]) {}

object ReadResponse {
  case class ItemFeatures(key: String, features: List[FeatureValue])
}
