package io.findify.featury.flink.feature

import io.findify.featury.flink.StateTTL
import io.findify.featury.flink.util.InitContext
import io.findify.featury.model.Feature.PeriodicCounter
import io.findify.featury.model.FeatureConfig.{CounterConfig, PeriodicCounterConfig}
import io.findify.featury.model.PeriodicCounterState.TimeCounter
import io.findify.featury.model.{Key, PeriodicCounterState, PeriodicCounterValue, Timestamp, Write}
import org.apache.flink.api.common.state.{KeyedStateStore, MapState, MapStateDescriptor, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation

import scala.collection.JavaConverters._

case class FlinkPeriodicCounter(config: PeriodicCounterConfig, counters: MapState[Long, Long]) extends PeriodicCounter {
  override def put(action: Write.PeriodicIncrement): Unit = {
    val key = action.ts.toStartOfPeriod(config.period)
    Option(counters.get(key.ts)) match {
      case None           => counters.put(key.ts, 1L)
      case Some(previous) => counters.put(key.ts, previous + 1L)
    }
  }

  override def computeValue(key: Key, ts: Timestamp): Option[PeriodicCounterValue] = {
    val map = counters.entries().asScala.map(e => Timestamp(e.getKey) -> e.getValue).toMap
    fromMap(map) match {
      case Nil    => None
      case values => Some(PeriodicCounterValue(key, ts, values))
    }
  }

  override def readState(key: Key, ts: Timestamp): Option[PeriodicCounterState] = {
    val values = counters.entries().asScala.map(e => TimeCounter(Timestamp(e.getKey), e.getValue))
    values.toList match {
      case Nil      => None
      case nonEmpty => Some(PeriodicCounterState(key, ts, nonEmpty))
    }
  }

  override def writeState(state: PeriodicCounterState): Unit = {
    state.values.foreach(tc => counters.put(tc.ts.ts, tc.count))
  }
}

object FlinkPeriodicCounter {
  def apply(ctx: InitContext, config: PeriodicCounterConfig)(implicit
      ti: TypeInformation[Long]
  ): FlinkPeriodicCounter = {
    val desc = new MapStateDescriptor[Long, Long](config.fqdn, ti, ti)
    desc.enableTimeToLive(StateTTL(config.ttl))
    FlinkPeriodicCounter(config, ctx.getMapState(desc))
  }

}
