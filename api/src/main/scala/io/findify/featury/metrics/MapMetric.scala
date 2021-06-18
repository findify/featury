package io.findify.featury.metrics

import io.findify.featury.model.FeatureConfig.MapConfig
import io.findify.featury.model.{FeatureKey, MapValue, SDouble}
import io.prometheus.client.Histogram

case class MapMetric(configs: Map[FeatureKey, MapConfig]) extends FeatureMetric {
  lazy val values = for {
    (key, conf) <- configs
    watchConfig <- conf.monitorValues
  } yield {
    key -> Histogram
      .build(s"featury_${key.ns.value}_${key.scope.value}_${key.feature.value}_value", "histogram of values")
      .buckets(buckets(watchConfig.min, watchConfig.max, watchConfig.buckets): _*)
      .register()
  }

  lazy val sizes = for {
    (key, conf) <- configs if conf.monitorSize
  } yield {
    key -> Histogram
      .build(s"featury_${key.ns.value}_${key.scope.value}_${key.feature.value}_size", "size of map")
      .buckets(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024)
      .create()
  }

  override def observe = { case MapValue(key, ts, vals) =>
    observeLag(key, ts)
    sizes.get(FeatureKey(key)).foreach(_.observe(values.size))
    for {
      valueHist <- values.get(FeatureKey(key))
      num       <- vals.values.collect { case SDouble(num) => num }
    } {
      valueHist.observe(num)
    }
  }
}
