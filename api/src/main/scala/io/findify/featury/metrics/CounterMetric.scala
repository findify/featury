package io.findify.featury.metrics

import io.findify.featury.model.FeatureConfig.CounterConfig
import io.findify.featury.model.{CounterValue, FeatureKey}
import io.prometheus.client.Histogram

import scala.collection.mutable.ArrayBuffer

case class CounterMetric(configs: Map[FeatureKey, CounterConfig]) extends FeatureMetric {
  lazy val values = for {
    (key, conf) <- configs
    watchConfig <- conf.monitorValues
  } yield {
    key -> Histogram
      .build(s"featury_${key.ns.value}_${key.scope.name}_${key.feature.value}_value", "histogram of values")
      .buckets(buckets(watchConfig.min, watchConfig.max, watchConfig.buckets): _*)
      .register()
  }

  override def observe = { case CounterValue(key, ts, value) =>
    values.get(FeatureKey(key)).foreach(_.observe(value))
    observeLag(key, ts)
  }
}
