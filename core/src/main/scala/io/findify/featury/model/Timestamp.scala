package io.findify.featury.model

import java.text.SimpleDateFormat
import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.FiniteDuration

case class Timestamp(ts: Long) {
  def isBefore(right: Timestamp)         = ts < right.ts
  def isBeforeOrEquals(right: Timestamp) = ts <= right.ts
  def isAfter(right: Timestamp)          = ts > right.ts
  def isAfterOrEquals(right: Timestamp)  = ts >= right.ts
  def plus(d: FiniteDuration)            = Timestamp(ts + d.toMillis)
  def minus(d: FiniteDuration)           = Timestamp(ts - d.toMillis)
  def toStartOfPeriod(period: FiniteDuration) = {
    val p = math.floor(ts.toDouble / period.toMillis).toLong
    Timestamp(p * period.toMillis)
  }

  override def toString: String = Instant.ofEpochMilli(ts).atOffset(ZoneOffset.UTC).format(Timestamp.format)
}

object Timestamp {
  val format        = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val MILLIS_IN_DAY = 1000L * 60 * 60 * 24
  def now           = new Timestamp(System.currentTimeMillis())
}
