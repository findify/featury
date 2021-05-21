package io.findify.featury.model

sealed trait Write {
  def key: Key
  def ts: Timestamp
}
object Write {
  def selectIncrement: PartialFunction[Write, Increment] = { case w: Increment =>
    w
  }
  def selectString: PartialFunction[Write, PutString] = { case w: PutString =>
    w
  }
  def selectDouble: PartialFunction[Write, PutDouble] = { case w: PutDouble =>
    w
  }
  sealed trait Put[T <: Scalar] extends Write {
    def value: T
  }
  object Put {
    def apply(key: Key, ts: Timestamp, value: SString) = PutString(key, ts, value)
    def apply(key: Key, ts: Timestamp, value: SDouble) = PutDouble(key, ts, value)
  }
  case class PutString(key: Key, ts: Timestamp, value: SString) extends Put[SString]
  case class PutDouble(key: Key, ts: Timestamp, value: SDouble) extends Put[SDouble]

  case class Increment(key: Key, ts: Timestamp, inc: Int)         extends Write
  case class PeriodicIncrement(key: Key, ts: Timestamp, inc: Int) extends Write

  case class Append[T <: Scalar](key: Key, value: T, ts: Timestamp) extends Write

  case class PutStatSample(key: Key, ts: Timestamp, value: Double) extends Write
  case class PutFreqSample(key: Key, ts: Timestamp, value: String) extends Write
}
