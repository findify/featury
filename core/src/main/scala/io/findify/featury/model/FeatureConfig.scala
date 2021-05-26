package io.findify.featury.model

import io.findify.featury.model.Key._

import scala.concurrent.duration._

sealed trait FeatureConfig {
  def ns: Namespace
  def group: GroupName
  def name: FeatureName
  def ttl: FiniteDuration
  def refresh: FiniteDuration
  def fqdn = s"${ns.value}.${group.value}.${name.value}"
}

object FeatureConfig {
  case class CounterConfig(
      name: FeatureName,
      ns: Namespace,
      group: GroupName,
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour
  ) extends FeatureConfig

  case class ScalarConfig(
      name: FeatureName,
      ns: Namespace,
      group: GroupName,
      contentType: ScalarType,
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour
  ) extends FeatureConfig

  case class BoundedListConfig(
      name: FeatureName,
      ns: Namespace,
      group: GroupName,
      count: Int = Int.MaxValue,
      duration: FiniteDuration = Long.MaxValue.nanos,
      //contentType: ScalarType,
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour
  ) extends FeatureConfig

  case class FreqEstimatorConfig(
      name: FeatureName,
      ns: Namespace,
      group: GroupName,
      poolSize: Int,
      sampleRate: Int,
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour
  ) extends FeatureConfig

  case class PeriodRange(startOffset: Int, endOffset: Int)
  case class PeriodicCounterConfig(
      name: FeatureName,
      ns: Namespace,
      group: GroupName,
      period: FiniteDuration,
      count: Int,
      sumPeriodRanges: List[PeriodRange],
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour
  ) extends FeatureConfig {
    private val periods      = (sumPeriodRanges.map(_.startOffset) ++ sumPeriodRanges.map(_.endOffset)).sorted
    val latestPeriodOffset   = periods.head
    val earliestPeriodOffset = periods.last
  }

  case class StatsEstimatorConfig(
      name: FeatureName,
      ns: Namespace,
      group: GroupName,
      poolSize: Int,
      sampleRate: Int,
      percentiles: List[Int],
      ttl: FiniteDuration = 365.days,
      refresh: FiniteDuration = 1.hour
  ) extends FeatureConfig

}
