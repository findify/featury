package io.findify.featury.flink

import io.findify.featury.flink.feature._
import io.findify.featury.flink.util.ScopeKey
import io.findify.featury.model.Feature._
import io.findify.featury.model.FeatureConfig._
import io.findify.featury.model.Write.{Append, Increment, PeriodicIncrement, Put, PutFreqSample, PutStatSample}
import io.findify.featury.model.{Schema, _}
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.state.KeyedStateStore
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala.extensions._

object FeaturyFlow {
  import io.findify.featury.flink.util.StreamName._
  import FeatureTypeInfo._
  import io.findify.flinkadt.api._

  def join[T](
      values: DataStream[FeatureValue],
      events: DataStream[T],
      scope: T => ScopeKey,
      add: (T, List[FeatureValue]) => T
  )(implicit ti: TypeInformation[T]) = {
    events
      .connect(values)
      .keyBy[ScopeKey](t => scope(t), value => ScopeKey(value.key))
      .process(new FeatureJoinFunction[T](add))
  }

  def process(stream: DataStream[Write], schema: Schema): DataStream[FeatureValue] = {
    def add[T <: FeatureValue, C <: FeatureConfig](
        stream: DataStream[Write],
        conf: Map[FeatureKey, C],
        make: (DataStream[Write], Map[FeatureKey, C]) => DataStream[T]
    ) =
      if (conf.isEmpty) None else Some(make(stream, conf).mapWith(x => x.asInstanceOf[FeatureValue]))

    val targets = List(
      add(stream, schema.counters, processCounters),
      add(stream, schema.lists, processList),
      add(stream, schema.stats, processStatEstimators),
      add(stream, schema.scalars, processScalar),
      add(stream, schema.freqs, processFreqEstimators),
      add(stream, schema.periodicCounters, processPeriodicCounters)
    ).flatten
    targets.reduceLeft((a, b) => a.union(b)).assignAscendingTimestamps(_.ts.ts)
  }

  def processCounters(stream: DataStream[Write], configs: Map[FeatureKey, CounterConfig]): DataStream[CounterValue] = {
    processFeature[Increment, CounterConfig, CounterValue, CounterState, Counter](
      stream = stream,
      "counters",
      configs,
      FlinkCounter.apply,
      Write.selectIncrement
    )
  }

  def processFreqEstimators(
      stream: DataStream[Write],
      configs: Map[FeatureKey, FreqEstimatorConfig]
  ): DataStream[FrequencyValue] = {
    processFeature[PutFreqSample, FreqEstimatorConfig, FrequencyValue, FrequencyState, FreqEstimator](
      stream = stream,
      "freq",
      configs,
      FlinkFreqEstimator.apply,
      Write.selectFreq
    )
  }

  def processStatEstimators(
      stream: DataStream[Write],
      configs: Map[FeatureKey, StatsEstimatorConfig]
  ): DataStream[NumStatsValue] = {
    processFeature[PutStatSample, StatsEstimatorConfig, NumStatsValue, StatsState, StatsEstimator](
      stream = stream,
      "stats",
      configs,
      FlinkStatsEstimator.apply,
      Write.selectStats
    )
  }

  def processPeriodicCounters(
      stream: DataStream[Write],
      configs: Map[FeatureKey, PeriodicCounterConfig]
  ): DataStream[PeriodicCounterValue] = {
    processFeature[
      PeriodicIncrement,
      PeriodicCounterConfig,
      PeriodicCounterValue,
      PeriodicCounterState,
      PeriodicCounter
    ](
      stream = stream,
      "periodic_counters",
      configs,
      FlinkPeriodicCounter.apply,
      Write.selectPeriodicIncrement
    )
  }

  def processScalar(stream: DataStream[Write], configs: Map[FeatureKey, ScalarConfig]): DataStream[ScalarValue] = {
    processFeature[Put, ScalarConfig, ScalarValue, ScalarState, ScalarFeature](
      stream = stream,
      name = "scalars",
      configs = configs,
      FlinkScalarFeature.apply,
      Write.selectPut
    )
  }

  def processList(
      stream: DataStream[Write],
      configs: Map[FeatureKey, BoundedListConfig]
  ): DataStream[BoundedListValue] = {
    processFeature[Append, BoundedListConfig, BoundedListValue, BoundedListState, BoundedList](
      stream = stream,
      name = "lists",
      configs = configs,
      FlinkBoundedList.apply,
      Write.selectAppend
    )
  }

  def processFeature[
      W <: Write: TypeInformation,
      C <: FeatureConfig,
      T <: FeatureValue: TypeInformation,
      S <: State,
      F <: Feature[W, T, C, S]
  ](
      stream: DataStream[Write],
      name: String,
      configs: Map[FeatureKey, C],
      make: (KeyedStateStore, C) => F,
      select: PartialFunction[Write, W]
  ): DataStream[T] = {
    stream
      .assignTimestampsAndWatermarks(
        WatermarkStrategy
          .forBoundedOutOfOrderness[Write](java.time.Duration.ofSeconds(0))
          .withTimestampAssigner(new SerializableTimestampAssigner[Write] {
            override def extractTimestamp(element: Write, recordTimestamp: Long): Long = element.ts.ts
          })
      )
      .flatMapWith(w => select.lift(w))
      .id(s"select-${name}")
      .keyingBy(_.key)
      .process(
        new FeatureProcessFunction[W, T, C, S, F](
          configs = configs,
          name = name,
          make = make
        )
      )
      .id(s"process-${name}")
  }

}
