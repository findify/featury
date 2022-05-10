package io.findify.featury.flink.feature

import io.findify.featury.flink.StateTTL
import io.findify.featury.flink.util.InitContext
import io.findify.featury.model.Feature.StatsEstimator
import io.findify.featury.model.FeatureConfig.{FreqEstimatorConfig, StatsEstimatorConfig}
import io.findify.featury.model.{FrequencyValue, Key, NumStatsValue, StatsState, Timestamp, Write}
import org.apache.flink.api.common.state.{
  KeyedStateStore,
  ListState,
  ListStateDescriptor,
  ValueState,
  ValueStateDescriptor
}
import org.apache.flink.api.common.typeinfo.TypeInformation

import scala.jdk.CollectionConverters._

case class FlinkStatsEstimator(config: StatsEstimatorConfig, list: ListState[Double], size: ValueState[Int])
    extends StatsEstimator {
  override def putSampled(action: Write.PutStatSample): Unit = {
    list.add(action.value)
    val newSize = Option(size.value()).getOrElse(0) + 1
    size.update(newSize)
    if (newSize > config.poolSize * 2) {
      val pool = list.get().asScala.take(config.poolSize)
      list.update(java.util.List.copyOf(pool.asJavaCollection))
      size.update(pool.size)
    }
  }

  override def computeValue(key: Key, ts: Timestamp): Option[NumStatsValue] = list.get().asScala.toList match {
    case Nil      => None
    case nonEmpty => Some(fromPool(key, ts, nonEmpty))
  }

  override def writeState(state: StatsState): Unit = {
    list.update(state.values.asJava)
    size.update(state.values.size)
  }

  override def readState(key: Key, ts: Timestamp): Option[StatsState] = {
    list.get().asScala.toList match {
      case Nil => None
      case nel => Some(StatsState(key, ts, nel))
    }
  }
}

object FlinkStatsEstimator {
  def apply(ctx: InitContext, config: StatsEstimatorConfig)(implicit
      ts: TypeInformation[Double],
      ti: TypeInformation[Int]
  ): FlinkStatsEstimator = {
    val ttl  = StateTTL(config.ttl)
    val list = new ListStateDescriptor[Double](config.fqdn, ts)
    list.enableTimeToLive(ttl)
    val size = new ValueStateDescriptor[Int](config.fqdn + ".size", ti)
    size.enableTimeToLive(ttl)
    FlinkStatsEstimator(config, ctx.getListState(list), ctx.getState(size))
  }
}
