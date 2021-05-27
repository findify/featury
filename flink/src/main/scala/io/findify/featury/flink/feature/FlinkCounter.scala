package io.findify.featury.flink.feature

import io.findify.featury.flink.StateTTL
import io.findify.featury.model.Feature.Counter
import io.findify.featury.model.FeatureConfig.CounterConfig
import io.findify.featury.model.Write.Increment
import io.findify.featury.model.{CounterState, CounterValue, Key, SLong, Timestamp}
import org.apache.flink.api.common.state.{KeyedStateStore, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation

case class FlinkCounter(config: CounterConfig, valueState: ValueState[Long]) extends Counter {
  override def put(action: Increment): Unit = Option(valueState.value()) match {
    case Some(counter) =>
      valueState.update(counter + action.inc)
    case None =>
      valueState.update(action.inc)
  }

  override def computeValue(key: Key, ts: Timestamp): Option[CounterValue] =
    Option(valueState.value()).map(v => CounterValue(key, ts, v))

  override def writeState(state: CounterState): Unit = valueState.update(state.value)

  override def readState(key: Key, ts: Timestamp): Option[CounterState] =
    Option(valueState.value()).map(CounterState(key, ts, _))
}

object FlinkCounter {
  def apply(ctx: KeyedStateStore, config: CounterConfig)(implicit ti: TypeInformation[Long]): FlinkCounter = {
    val desc = new ValueStateDescriptor[Long](config.fqdn, ti)
    desc.enableTimeToLive(StateTTL(config.ttl))
    FlinkCounter(config, ctx.getState(desc))
  }
}
