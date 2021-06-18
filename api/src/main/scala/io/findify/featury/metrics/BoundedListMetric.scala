package io.findify.featury.metrics

import io.findify.featury.model.FeatureConfig.BoundedListConfig
import io.findify.featury.model.{BoundedListValue, FeatureKey}
import io.prometheus.client.Histogram

case class BoundedListMetric(configs: Map[FeatureKey, BoundedListConfig]) extends FeatureMetric {
  lazy val sizes = for {
    (key, conf) <- configs if conf.monitorSize.getOrElse(false)
  } yield {
    key -> Histogram
      .build(s"featury_${key.ns.value}_${key.scope.value}_${key.feature.value}_size", "size of map")
      .buckets(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024)
      .register()
  }

  override def observe = { case BoundedListValue(key, ts, values) =>
    sizes.get(FeatureKey(key)).foreach(_.observe(values.size))
    observeLag(key, ts)
  }
}
