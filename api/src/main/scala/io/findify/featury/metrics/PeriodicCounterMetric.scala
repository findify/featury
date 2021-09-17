package io.findify.featury.metrics

import io.findify.featury.model.FeatureConfig.PeriodicCounterConfig
import io.findify.featury.model.{FeatureKey, PeriodicCounterValue}
import io.prometheus.client.Histogram

case class PeriodicCounterMetric(configs: Map[FeatureKey, PeriodicCounterConfig]) extends FeatureMetric {
  lazy val periodsMap = for {
    (key, conf) <- configs
    watchConfig <- conf.monitorValues
  } yield {
    val percentiles = conf.periods
      .map(p =>
        p -> Histogram
          .build(s"featury_${key.scope.name}_${key.feature.value}_value_p$p", "histogram of values")
          .buckets(watchConfig.bucketList: _*)
          .register()
      )
      .toMap
    key -> percentiles
  }

  override def observe = { case PeriodicCounterValue(key, ts, values) =>
    observeLag(key, ts)
    for {
      periodHists <- periodsMap.get(FeatureKey(key))
      period      <- values
      periodHist  <- periodHists.get(period.periods)
    } {
      periodHist.observe(period.value)
    }
  }

}
