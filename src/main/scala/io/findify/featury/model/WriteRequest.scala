package io.findify.featury.model

import io.findify.featury.model.WriteRequest.WriteAction

case class WriteRequest(actions: List[WriteAction]) {}

object WriteRequest {
  sealed trait WriteAction {
    def key: Key
  }
  case class WriteCounter(key: Key, inc: Double)                   extends WriteAction
  case class WriteTextList(key: Key, ts: Timestamp, value: String) extends WriteAction
  case class WriteNumList(key: Key, ts: Timestamp, value: Double)  extends WriteAction
}
