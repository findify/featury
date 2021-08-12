package io.findify.featury.model

sealed trait Write {
  def key: Key
  def ts: Timestamp
}
object Write {
  case class Put(key: Key, ts: Timestamp, value: Scalar)                              extends Write
  case class PutTuple(key: Key, ts: Timestamp, mapKey: String, value: Option[Scalar]) extends Write

  case class Increment(key: Key, ts: Timestamp, inc: Int)         extends Write
  case class PeriodicIncrement(key: Key, ts: Timestamp, inc: Int) extends Write

  case class Append(key: Key, value: Scalar, ts: Timestamp) extends Write

  case class PutStatSample(key: Key, ts: Timestamp, value: Double) extends Write
  case class PutFreqSample(key: Key, ts: Timestamp, value: String) extends Write
}
