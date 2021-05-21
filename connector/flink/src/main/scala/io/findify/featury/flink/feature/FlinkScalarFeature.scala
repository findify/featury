package io.findify.featury.flink.feature

import io.findify.featury.flink.StateTTL
import io.findify.featury.model.Feature.ScalarFeature
import io.findify.featury.model.FeatureConfig.ScalarConfig
import io.findify.featury.model.FeatureValue.ScalarValue.{DoubleScalarValue, StringScalarValue}
import io.findify.featury.model.FeatureValue.ScalarValue
import io.findify.featury.model.Write.Put
import io.findify.featury.model.{FeatureValue, Key, SDouble, SString, Scalar}
import org.apache.flink.api.common.state.{KeyedStateStore, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation

trait FlinkScalarFeature[T <: Scalar] extends ScalarFeature[T] {
  def state: ValueState[T]
  override def put(action: Put[T]): Unit = state.update(action.value)

  override def computeValue(key: Key): Option[FeatureValue.ScalarValue[T]] =
    Option(state.value()).map(makeValue)
}

object FlinkScalarFeature {
  case class FlinkStringScalarFeature(config: ScalarConfig, state: ValueState[SString])
      extends FlinkScalarFeature[SString] {
    override def makeValue(value: SString): ScalarValue[SString] = StringScalarValue(value)
  }

  case class FlinkDoubleScalarFeature(config: ScalarConfig, state: ValueState[SDouble])
      extends FlinkScalarFeature[SDouble] {
    override def makeValue(value: SDouble): ScalarValue[SDouble] = DoubleScalarValue(value)
  }

  def applyString(ctx: KeyedStateStore, config: ScalarConfig)(implicit
      ti: TypeInformation[SString]
  ): FlinkStringScalarFeature = {
    val desc = new ValueStateDescriptor[SString](config.fqdn, ti)
    desc.enableTimeToLive(StateTTL(config.ttl))
    FlinkStringScalarFeature(config, ctx.getState(desc))
  }

  def applyDouble(ctx: KeyedStateStore, config: ScalarConfig)(implicit
      ti: TypeInformation[SDouble]
  ): FlinkDoubleScalarFeature = {
    val desc = new ValueStateDescriptor[SDouble](config.fqdn, ti)
    desc.enableTimeToLive(StateTTL(config.ttl))
    FlinkDoubleScalarFeature(config, ctx.getState(desc))
  }
}
