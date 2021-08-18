package io.findify.featury.flink.feature

import io.findify.featury.flink.StateTTL
import io.findify.featury.flink.util.InitContext
import io.findify.featury.model.Feature.ScalarFeature
import io.findify.featury.model.FeatureConfig.ScalarConfig
import io.findify.featury.model.Write.Put
import io.findify.featury.model._
import org.apache.flink.api.common.state.{KeyedStateStore, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation

case class FlinkScalarFeature(config: ScalarConfig, valueState: ValueState[Scalar]) extends ScalarFeature {
  override def put(action: Put): Unit = {
    valueState.update(action.value)
  }

  override def computeValue(key: Key, ts: Timestamp): Option[ScalarValue] = {
    val read = Option(valueState.value())
    read.map(ScalarValue(key, ts, _))
  }

  override def readState(key: Key, ts: Timestamp): Option[ScalarState] =
    Option(valueState.value()).map(ScalarState(key, ts, _))

  override def writeState(state: ScalarState): Unit = valueState.update(state.value)
}

object FlinkScalarFeature {

  def apply(ctx: InitContext, config: ScalarConfig)(implicit
      ti: TypeInformation[Scalar]
  ): FlinkScalarFeature = {
    val desc = new ValueStateDescriptor[Scalar](config.fqdn, ti)
    desc.enableTimeToLive(StateTTL(config.ttl))
    FlinkScalarFeature(config, ctx.getState(desc))
  }
}
