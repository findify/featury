package io.findify.featury.flink

import io.findify.featury.flink.util.InitContext.DataSetContext
import io.findify.featury.model.{
  BoundedListState,
  CounterState,
  Feature,
  FeatureKey,
  FrequencyState,
  Key,
  MapState,
  PeriodicCounterState,
  Scalar,
  ScalarState,
  Schema,
  State,
  StatsState,
  TimeValue
}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.state.api.functions.KeyedStateBootstrapFunction
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
    features.get(FeatureKey(value.key)) match {
      case None => LOG.warn(s"no feature configured for a key ${value.key} of state $value")
      case Some(feature) =>
        (feature, value) match {
          case (scalar: Feature.ScalarFeature, state: ScalarState)             => scalar.writeState(state)
          case (map: Feature.MapFeature, state: MapState)                      => map.writeState(state)
          case (counter: Feature.Counter, state: CounterState)                 => counter.writeState(state)
          case (list: Feature.BoundedList, state: BoundedListState)            => list.writeState(state)
          case (freq: Feature.FreqEstimator, state: FrequencyState)            => freq.writeState(state)
          case (counter: Feature.PeriodicCounter, state: PeriodicCounterState) => counter.writeState(state)
          case (stats: Feature.StatsEstimator, state: StatsState)              => stats.writeState(state)
          case _ =>
            LOG.warn(s"received state $value for a feature $feature: type mismatch")
        }
    }
  }
}
