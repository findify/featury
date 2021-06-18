package io.findify.featury.metrics

import io.findify.featury.model.FeatureConfig.FreqEstimatorConfig
import io.findify.featury.model.{FeatureKey, FrequencyValue}
import io.prometheus.client.Histogram

case class FrequencyMetric(configs: Map[FeatureKey, FreqEstimatorConfig]) extends FeatureMetric {
  lazy val sizes = for {
    (key, conf) <- configs if conf.monitorSize.getOrElse(false)
  } yield {
    key -> Histogram
      .build(s"featury_${key.ns.value}_${key.scope.value}_${key.feature.value}_size", "size of map")
      .buckets(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024)
      .register()
  }

  override def observe = { case FrequencyValue(key, ts, values) =>
    sizes.get(FeatureKey(key)).foreach(_.observe(values.size))
    observeLag(key, ts)
  }
}
