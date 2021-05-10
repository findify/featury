package io.findify.featury.model

import io.findify.featury.model.Feature.Op
import io.findify.featury.model.WriteRequest.WriteAction

case class WriteRequest(actions: List[WriteAction]) {}

object WriteRequest {
  case class WriteAction(tenant: Int, group: String, feature: String, key: String, op: Op)
}
