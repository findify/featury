package io.findify.featury.model

import scala.concurrent.duration.FiniteDuration

case class Timestamp(ts: Long) extends AnyVal {
  def isBefore(right: Timestamp) = ts < right.ts
  def isAfter(right: Timestamp)  = ts > right.ts
  def plus(d: FiniteDuration)    = Timestamp(ts + d.toMillis)
  def minus(d: FiniteDuration)   = Timestamp(ts - d.toMillis)
}

object Timestamp {
  def now = new Timestamp(System.currentTimeMillis())
}
