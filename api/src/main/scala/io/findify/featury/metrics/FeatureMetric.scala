package io.findify.featury.metrics

import io.findify.featury.model.FeatureConfig.{
  CounterConfig,
  MapConfig,
  PeriodicCounterConfig,
  ScalarConfig,
  StatsEstimatorConfig
}
import io.findify.featury.model._
import io.prometheus.client.Histogram

import scala.collection.mutable.ArrayBuffer

trait FeatureMetric {
  val LAG_BUCKETS = List(0.01, 0.05, 0.1, 0.2, 0.5, 0.75, 0.9, 1.0, 1.1, 1.2, 1.5, 2.0, 4.0, 10.0, 20.0)

  lazy val lags = for {
    (key, conf) <- configs if conf.monitorLag
  } yield {
    val buckets = LAG_BUCKETS.map(lag => conf.refresh.toSeconds * lag)
    key -> Histogram
      .build(s"featury_${key.ns.value}_${key.scope.value}_${key.feature.value}_lag", "lag of values")
      .buckets(buckets: _*)
      .register()
  }

  def configs: Map[FeatureKey, FeatureConfig]
  def observe: PartialFunction[FeatureValue, Unit]

  protected def observeLag(key: Key, ts: Timestamp): Unit = {
    val delay = Timestamp.now.diff(ts).toSeconds
    lags.get(FeatureKey(key)).foreach(_.observe(delay))
  }

  protected def buckets(min: Double, max: Double, count: Int): List[Double] = {
    val buffer = ArrayBuffer[Double]()
    val step   = (max - min) / count
    var i      = min
    while (i <= max) {
      buffer.append(i)
      i += step
    }
    buffer.toList
  }

}
