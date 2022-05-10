package io.findify.featury.model.api

import io.circe.Codec
import io.circe.generic.semiauto._
import io.findify.featury.model.FeatureValue

case class ReadResponse(features: List[FeatureValue]) {}
