package io.findify.featury.flink.feature

import io.findify.featury.flink.StateTTL
import io.findify.featury.model.Feature.Counter
import io.findify.featury.model.FeatureConfig.CounterConfig
import io.findify.featury.model.FeatureValue.ScalarValue.LongScalarValue
import io.findify.featury.model.Write.Increment
import io.findify.featury.model.{FeatureValue, Key, SLong}
import org.apache.flink.api.common.state.{KeyedStateStore, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation

case class FlinkCounterFeature(config: CounterConfig, state: ValueState[Long]) extends Counter {
  override def put(action: Increment): Unit = {
    Option(state.value()) match {
      case Some(counter) => state.update(counter + action.inc)
      case None          => state.update(action.inc)
    }
  }

  override def computeValue(key: Key): Option[LongScalarValue] =
    Option(state.value()).map(v => LongScalarValue(SLong(v)))
}

object FlinkCounterFeature {
  def apply(ctx: KeyedStateStore, config: CounterConfig)(implicit ti: TypeInformation[Long]): FlinkCounterFeature = {
    val desc = new ValueStateDescriptor[Long](config.fqdn, ti)
    desc.enableTimeToLive(StateTTL(config.ttl))
    FlinkCounterFeature(config, ctx.getState(desc))
  }
}
