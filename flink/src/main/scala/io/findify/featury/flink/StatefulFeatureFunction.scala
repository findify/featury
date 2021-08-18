package io.findify.featury.flink

import io.findify.featury.flink.feature.{
  FlinkBoundedList,
  FlinkCounter,
  FlinkFreqEstimator,
  FlinkMapFeature,
  FlinkPeriodicCounter,
  FlinkScalarFeature,
  FlinkStatsEstimator
}
import io.findify.featury.flink.util.InitContext
import io.findify.featury.model.FeatureConfig.{
  BoundedListConfig,
  CounterConfig,
  FreqEstimatorConfig,
  MapConfig,
  PeriodicCounterConfig,
  ScalarConfig,
  StatsEstimatorConfig
}
import io.findify.featury.model.{
  Feature,
  FeatureConfig,
  FeatureKey,
  FeatureValue,
  Scalar,
  Schema,
  State,
  TimeValue,
  Write
}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation

/** A trait to share state definition between FeatureProcessFunction (which is offline/online) and
  * FeatureBootstrapFunction (which is able to write savepoint from offline to online)
  */
trait StatefulFeatureFunction {

  @transient var features: Map[FeatureKey, Feature[_ <: Write, _ <: FeatureValue, _ <: FeatureConfig, _ <: State]] = _
  @transient var updated: ValueState[Long]                                                                         = _

  def init(schema: Schema, context: InitContext)(implicit
      longTI: TypeInformation[Long],
      intTI: TypeInformation[Int],
      doubleTI: TypeInformation[Double],
      tvTI: TypeInformation[TimeValue],
      stringTI: TypeInformation[String],
      scalarTI: TypeInformation[Scalar],
      stateTI: TypeInformation[State]
  ) = {
    features = schema.configs.map {
      case (key, c: CounterConfig)         => key -> FlinkCounter(context, c)
      case (key, c: PeriodicCounterConfig) => key -> FlinkPeriodicCounter(context, c)
      case (key, c: BoundedListConfig)     => key -> FlinkBoundedList(context, c)
      case (key, c: FreqEstimatorConfig)   => key -> FlinkFreqEstimator(context, c)
      case (key, c: ScalarConfig)          => key -> FlinkScalarFeature(context, c)
      case (key, c: StatsEstimatorConfig)  => key -> FlinkStatsEstimator(context, c)
      case (key, c: MapConfig)             => key -> FlinkMapFeature(context, c)
    }
    updated = context.getState(
      new ValueStateDescriptor[Long]("last-update", implicitly[TypeInformation[Long]])
    )

  }
}
