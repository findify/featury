package io.findify.featury.metrics

import io.findify.featury.model.FeatureConfig.ScalarConfig
import io.findify.featury.model.{FeatureKey, SDouble, ScalarValue}
import io.prometheus.client.Histogram

case class ScalarMetric(configs: Map[FeatureKey, ScalarConfig]) extends FeatureMetric {
  lazy val values = for {
    (key, conf) <- configs
    watchConfig <- conf.monitorValues
  } yield {
    key -> Histogram
      .build(s"featury_${key.scope.name}_${key.feature.value}_value", "histogram of values")
      .buckets(buckets(watchConfig.min, watchConfig.max, watchConfig.buckets): _*)
      .register()
  }

  override def observe = {
    case ScalarValue(key, ts, SDouble(value)) =>
      values.get(FeatureKey(key)).foreach(_.observe(value))
      observeLag(key, ts)
    case ScalarValue(key, ts, _) =>
      observeLag(key, ts)
  }
}
