package io.findify.featury.feature

import cats.effect.IO
import io.findify.featury.feature.Feature.State
import io.findify.featury.feature.FreqEstimator.{FreqEstimatorConfig, FreqEstimatorState}
import io.findify.featury.model.FeatureValue.StringFrequencyValue
import io.findify.featury.model.Key
import io.findify.featury.model.Key.FeatureName
import io.findify.featury.model.Schema.FeatureConfig

import scala.util.Random

trait FreqEstimator extends Feature[FreqEstimatorState, StringFrequencyValue, FreqEstimatorConfig] {
  def config: FreqEstimatorConfig
  def put(key: Key, value: String): IO[Unit] = if (Random.nextInt(config.sampleRate) == 0) {
    putReal(key, value)
  } else {
    IO.unit
  }

  def putReal(key: Key, value: String): IO[Unit]

  override def empty(): FreqEstimatorState = FreqEstimatorState(Vector.empty)
  override def computeValue(state: FreqEstimatorState): Option[StringFrequencyValue] = {
    if (state.samples.isEmpty) {
      None
    } else {
      val count = state.samples.size
      Some(StringFrequencyValue(state.samples.groupBy(identity).map { case (key, value) =>
        key -> value.size.toDouble / count
      }))
    }
  }
}

object FreqEstimator {
  case class FreqEstimatorState(samples: Vector[String])                            extends State
  case class FreqEstimatorConfig(name: FeatureName, poolSize: Int, sampleRate: Int) extends FeatureConfig
}
