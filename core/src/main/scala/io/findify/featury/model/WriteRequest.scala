package io.findify.featury.model

import io.findify.featury.model.FeatureValue.{Num, Scalar, Text}
import io.findify.featury.model.WriteRequest.WriteAction

case class WriteRequest(actions: List[WriteAction]) {}

object WriteRequest {
  sealed trait WriteAction {
    def key: Key
  }
  case class Put[T <: Scalar](key: Key, value: T)                 extends WriteAction
  case class Increment(key: Key, inc: Int)                        extends WriteAction
  case class PeriodicIncrement(key: Key, ts: Timestamp, inc: Int) extends WriteAction

  case class Append[T <: Scalar](key: Key, value: T, ts: Timestamp) extends WriteAction

  case class PutStatSample(key: Key, value: Double) extends WriteAction
  case class PutFreqSample(key: Key, value: String) extends WriteAction
}
