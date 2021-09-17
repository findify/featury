package io.findify.featury.api

import cats.effect.IO
import io.findify.featury.metrics._
import io.findify.featury.model._
import io.prometheus.client.{CollectorRegistry, Counter, Gauge, Histogram, Summary}
import io.prometheus.client.exporter.common.TextFormat
import org.http4s.HttpRoutes
import org.http4s.Uri.Path.Segment
import org.http4s.dsl.io._

import java.io.{ByteArrayOutputStream, OutputStreamWriter}

case class MetricsApi(schema: Schema) {
  lazy val registry = CollectorRegistry.defaultRegistry

  val scalarMetrics          = ScalarMetric(schema.scalars)
  val counterMetrics         = CounterMetric(schema.counters)
  val periodicCounterMetrics = PeriodicCounterMetric(schema.periodicCounters)
  val numStatMetrics         = NumStatsMetric(schema.stats)
  val mapMetrics             = MapMetric(schema.maps)
  val listMetrics            = BoundedListMetric(schema.lists)
  val freqMetrics            = FrequencyMetric(schema.freqs)

  val route = HttpRoutes.of[IO] { case GET -> Root / "metrics" =>
    Ok(writeStream(registry))
  }

  private def writeStream(registry: CollectorRegistry) = {
    val stream = new ByteArrayOutputStream()
    val writer = new OutputStreamWriter(stream)
    TextFormat.write004(writer, registry.metricFamilySamples())
    writer.close()
    stream.toByteArray
  }

  def collectFeatureValues(value: FeatureValue): Unit = value match {
    case x: ScalarValue          => scalarMetrics.observe.lift(x)
    case x: CounterValue         => counterMetrics.observe.lift(x)
    case x: NumStatsValue        => numStatMetrics.observe.lift(x)
    case x: MapValue             => mapMetrics.observe.lift(x)
    case x: PeriodicCounterValue => periodicCounterMetrics.observe.lift(x)
    case x: FrequencyValue       => freqMetrics.observe.lift(x)
    case x: BoundedListValue     => listMetrics.observe.lift(x)
  }

  def keyLabels(key: Key): List[String] = {
    List(key.tag.scope.name, key.tenant.value, key.name.value)
  }

}
