package io.findify.featury.api

import io.findify.featury.model.FeatureConfig.{MonitorValuesConfig, ScalarConfig}
import io.findify.featury.model.Key.{FeatureName, Id, Namespace, Scope, Tenant}
import io.findify.featury.model.{Key, SDouble, ScalarValue, Schema, Timestamp}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.collection.JavaConverters._

class MetricsApiTest extends AnyFlatSpec with Matchers {
  it should "collect scalar metrics" in {
    val key = Key(
      ns = Namespace("ns"),
      scope = Scope("s"),
      name = FeatureName("f1"),
      tenant = Tenant("1"),
      id = Id("a")
    )
    val schema = Schema(
      ScalarConfig(
        ns = key.ns,
        scope = key.scope,
        name = key.name,
        monitorValues = Some(MonitorValuesConfig(0, 1, 10)),
        monitorLag = true
      )
    )
    val metrics = MetricsApi(schema)
    metrics.collectFeatureValues(ScalarValue(key, Timestamp.now, SDouble(3.0)))
    val samples = metrics.registry.metricFamilySamples().asIterator().asScala.flatMap(_.samples.asScala).toList
    samples.exists(_.name == "featury_ns_s_f1_value_bucket") shouldBe true
    samples.exists(_.name == "featury_ns_s_f1_lag_bucket") shouldBe true
  }
}
