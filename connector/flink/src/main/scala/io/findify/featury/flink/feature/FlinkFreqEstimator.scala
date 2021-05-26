package io.findify.featury.flink.feature

import io.findify.featury.flink.StateTTL
import io.findify.featury.model.Feature.FreqEstimator
import io.findify.featury.model.FeatureConfig.{CounterConfig, FreqEstimatorConfig}
import io.findify.featury.model.{Feature, FrequencyState, FrequencyValue, Key, Timestamp, Write}
import org.apache.flink.api.common.state.{
  KeyedStateStore,
  ListState,
  ListStateDescriptor,
  ValueState,
  ValueStateDescriptor
}
import org.apache.flink.api.common.typeinfo.TypeInformation

import scala.collection.JavaConverters._

case class FlinkFreqEstimator(config: FreqEstimatorConfig, list: ListState[String], size: ValueState[Int])
    extends FreqEstimator {

  override def putSampled(action: Write.PutFreqSample): Unit = {
    list.add(action.value)
    val newSize = Option(size.value()).getOrElse(0) + 1
    size.update(newSize)
    if (newSize > config.poolSize * 2) {
      val pool = list.get().asScala.take(config.poolSize)
      list.update(java.util.List.copyOf(pool.asJavaCollection))
      size.update(pool.size)
    }
  }

  override def computeValue(key: Key, ts: Timestamp): Option[FrequencyValue] = {
    list.get().asScala.toList match {
      case Nil => None
      case nonEmpty =>
        val size = nonEmpty.size.toDouble
        Some(FrequencyValue(key, ts, nonEmpty.groupBy(identity).map { case (k, v) => k -> v.size / size }))
    }
  }

  override def writeState(state: FrequencyState): Unit = {
    list.update(state.values.asJava)
    size.update(state.values.size)
  }

  override def readState(key: Key, ts: Timestamp): Option[FrequencyState] = {
    list.get().asScala.toList match {
      case Nil => None
      case nel => Some(FrequencyState(key, ts, nel))
    }
  }
}

object FlinkFreqEstimator {
  def apply(ctx: KeyedStateStore, config: FreqEstimatorConfig)(implicit
      ts: TypeInformation[String],
      ti: TypeInformation[Int]
  ): FlinkFreqEstimator = {
    val ttl  = StateTTL(config.ttl)
    val list = new ListStateDescriptor[String](config.fqdn, ts)
    list.enableTimeToLive(ttl)
    val size = new ValueStateDescriptor[Int](config.fqdn + ".size", ti)
    size.enableTimeToLive(ttl)
    FlinkFreqEstimator(config, ctx.getListState(list), ctx.getState(size))
  }

}
