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
import io.findify.featury.model.Feature.{
  BoundedList,
  Counter,
  FreqEstimator,
  MapFeature,
  PeriodicCounter,
  ScalarFeature,
  StatsEstimator
}
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

  @transient var counters: Map[FeatureKey, Counter]                 = _
  @transient var periodicCounters: Map[FeatureKey, PeriodicCounter] = _
  @transient var lists: Map[FeatureKey, BoundedList]                = _
  @transient var freqs: Map[FeatureKey, FreqEstimator]              = _
  @transient var scalars: Map[FeatureKey, ScalarFeature]            = _
  @transient var stats: Map[FeatureKey, StatsEstimator]             = _
  @transient var maps: Map[FeatureKey, MapFeature]                  = _
  @transient var updated: ValueState[Long]                          = _

  def init(schema: Schema, context: InitContext)(implicit
      longTI: TypeInformation[Long],
      intTI: TypeInformation[Int],
      doubleTI: TypeInformation[Double],
      tvTI: TypeInformation[TimeValue],
      stringTI: TypeInformation[String],
      scalarTI: TypeInformation[Scalar],
      stateTI: TypeInformation[State]
  ) = {
    counters = schema.counters.map { case (key, c) => key -> FlinkCounter(context, c) }
    periodicCounters = schema.periodicCounters.map { case (key, c) => key -> FlinkPeriodicCounter(context, c) }
    lists = schema.lists.map { case (key, c) => key -> FlinkBoundedList(context, c) }
    freqs = schema.freqs.map { case (key, c) => key -> FlinkFreqEstimator(context, c) }
    scalars = schema.scalars.map { case (key, c) => key -> FlinkScalarFeature(context, c) }
    stats = schema.stats.map { case (key, c) => key -> FlinkStatsEstimator(context, c) }
    maps = schema.maps.map { case (key, c) => key -> FlinkMapFeature(context, c) }

    updated = context.getState(
      new ValueStateDescriptor[Long]("last-update", implicitly[TypeInformation[Long]])
    )

  }
}
