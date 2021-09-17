package io.findify.featury.flink

import io.findify.featury.flink.util.InitContext.DataSetContext
import io.findify.featury.model.{
  BoundedListState,
  CounterState,
  Feature,
  FeatureConfig,
  FeatureKey,
  FeatureValue,
  FrequencyState,
  Key,
  MapState,
  PeriodicCounterState,
  Scalar,
  ScalarState,
  Schema,
  State,
  StatsState,
  TimeValue,
  Write
}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.state.api.functions.KeyedStateBootstrapFunction
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.LoggerFactory

class FeatureBootstrapFunction(schema: Schema)(implicit
    longTI: TypeInformation[Long],
    intTI: TypeInformation[Int],
    doubleTI: TypeInformation[Double],
    tvTI: TypeInformation[TimeValue],
    stringTI: TypeInformation[String],
    scalarTI: TypeInformation[Scalar],
    stateTI: TypeInformation[State]
) extends KeyedStateBootstrapFunction[Key, State]
    with StatefulFeatureFunction {

  private val LOG = LoggerFactory.getLogger(classOf[FeatureBootstrapFunction])

  override def open(parameters: Configuration): Unit = {
    init(schema, DataSetContext(getRuntimeContext))
  }

  override def processElement(value: State, ctx: KeyedStateBootstrapFunction[Key, State]#Context): Unit = {
    value match {
      case s: ScalarState          => writeState(scalars.get(FeatureKey(value.key)), s)
      case s: CounterState         => writeState(counters.get(FeatureKey(value.key)), s)
      case s: MapState             => writeState(maps.get(FeatureKey(value.key)), s)
      case s: PeriodicCounterState => writeState(periodicCounters.get(FeatureKey(value.key)), s)
      case s: BoundedListState     => writeState(lists.get(FeatureKey(value.key)), s)
      case s: FrequencyState       => writeState(freqs.get(FeatureKey(value.key)), s)
      case s: StatsState           => writeState(stats.get(FeatureKey(value.key)), s)
    }
  }

  def writeState[S <: State](
      featureOption: Option[Feature[_ <: Write, _ <: FeatureValue, _ <: FeatureConfig, S]],
      state: S
  ) = featureOption match {
    case Some(feature) =>
      feature.writeState(state)
    case None =>
      LOG.warn(s"no feature configured for a key ${state.key}")
  }
}
