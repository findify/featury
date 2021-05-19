package io.findify.featury.model

import io.findify.featury.model.WriteRequest.WriteAction

case class WriteRequest(actions: List[WriteAction]) {}

object WriteRequest {
  sealed trait WriteAction {
    def key: Key
  }
  case class SetText(key: Key, value: String)                     extends WriteAction
  case class SetNum(key: Key, value: Double)                      extends WriteAction
  case class Increment(key: Key, inc: Int)                        extends WriteAction
  case class PeriodicIncrement(key: Key, ts: Timestamp, inc: Int) extends WriteAction

  case class AppendText(key: Key, ts: Timestamp, value: String) extends WriteAction
  case class AppendNum(key: Key, ts: Timestamp, value: Double)  extends WriteAction

  case class PutStatSample(key: Key, value: Double) extends WriteAction
  case class PutFreqSample(key: Key, value: String) extends WriteAction
}
