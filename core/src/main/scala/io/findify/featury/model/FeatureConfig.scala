package io.findify.featury.model

import io.findify.featury.model.FeatureValue.ScalarType
import io.findify.featury.model.Key._

import scala.concurrent.duration._

sealed trait FeatureConfig {
  def ns: Namespace
  def group: GroupName
  def name: FeatureName
}

object FeatureConfig {
  case class CounterConfig(name: FeatureName, ns: Namespace, group: GroupName) extends FeatureConfig

  case class ScalarConfig(name: FeatureName, ns: Namespace, group: GroupName, contentType: ScalarType)
      extends FeatureConfig

  case class BoundedListConfig(
      name: FeatureName,
      ns: Namespace,
      group: GroupName,
      count: Int = Int.MaxValue,
      duration: FiniteDuration = Long.MaxValue.nanos,
      contentType: ScalarType
  ) extends FeatureConfig

  case class FreqEstimatorConfig(name: FeatureName, ns: Namespace, group: GroupName, poolSize: Int, sampleRate: Int)
      extends FeatureConfig

  case class PeriodRange(startOffset: Int, endOffset: Int)
  case class PeriodicCounterConfig(
      name: FeatureName,
      ns: Namespace,
      group: GroupName,
      period: FiniteDuration,
      count: Int,
      sumPeriodRanges: List[PeriodRange]
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
      percentiles: List[Int]
  ) extends FeatureConfig

}
