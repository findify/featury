package io.findify.featury.feature

import cats.effect.IO
import com.google.common.math.Quantiles
import io.findify.featury.feature.Feature.State
import io.findify.featury.feature.StatsEstimator.{StatsEstimatorConfig, StatsEstimatorState}
import io.findify.featury.model.FeatureValue.{Num, NumStatsValue}
import io.findify.featury.model.Key
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.Schema.FeatureConfig

import scala.collection.JavaConverters._
import scala.util.Random

trait StatsEstimator extends Feature[StatsEstimatorState, NumStatsValue, StatsEstimatorConfig] {
  def config: StatsEstimatorConfig
  final def put(key: Key, value: Double): IO[Unit] = {
    if (Random.nextInt(config.sampleRate) == 0) {
      putReal(key, value)
    } else {
      IO.unit
    }
  }

  def putReal(key: Key, value: Double): IO[Unit]

  override def computeValue(state: StatsEstimatorState): Option[NumStatsValue] = if (state.samples.isEmpty) {
    None
  } else {
    val quantile = Quantiles
      .percentiles()
      .indexes(config.percentiles.map(i => Integer.valueOf(i)).asJavaCollection)
      .compute(state.samples: _*)
      .asScala
      .map { case (k, v) =>
        k.intValue() -> v.doubleValue()
      }
    Some(
      NumStatsValue(
        min = state.samples.min,
        max = state.samples.max,
        quantiles = quantile.toMap
      )
    )
  }
}

object StatsEstimator {
  case class StatsEstimatorConfig(
      name: FeatureName,
      ns: Namespace,
      group: GroupName,
      poolSize: Int,
      sampleRate: Int,
      percentiles: List[Int]
  )                                                       extends FeatureConfig
  case class StatsEstimatorState(samples: Vector[Double]) extends State
}
