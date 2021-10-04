package io.findify.featury.metrics

import io.findify.featury.model.FeatureConfig.StatsEstimatorConfig
import io.findify.featury.model.{FeatureKey, NumStatsValue}
import io.prometheus.client.Histogram

case class NumStatsMetric(configs: Map[FeatureKey, StatsEstimatorConfig]) extends FeatureMetric {
  lazy val quantilesMap = for {
    (key, conf) <- configs
    watchConfig <- conf.monitorValues
  } yield {
    val percentiles = conf.percentiles
      .map(p =>
        p -> Histogram
          .build(s"featury_${key.scope.name}_${key.feature.value}_value_p$p", "histogram of values")
          .buckets(watchConfig.bucketList: _*)
          .register()
      )
      .toMap
    key -> percentiles
  }
  override def observe = { case NumStatsValue(key, ts, _, _, quantiles) =>
    observeLag(key, ts)
    for {
      (quantile, value) <- quantiles
      quantileHists     <- quantilesMap.get(FeatureKey(key))
      quantileHist      <- quantileHists.get(quantile)
    } {
      quantileHist.observe(value)
    }
  }
}
