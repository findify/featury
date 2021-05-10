package io.findify.featury.model

import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.ReadResponse.ItemFeatures

case class ReadResponse(items: List[ItemFeatures]) {}

object ReadResponse {
  case class ItemFeatures(key: Key, features: List[FeatureNameValue])
  case class FeatureNameValue(name: FeatureName, value: FeatureValue)
}
