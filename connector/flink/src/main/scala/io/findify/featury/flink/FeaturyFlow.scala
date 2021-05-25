package io.findify.featury.flink

import io.findify.featury.flink.feature.{FlinkCounterFeature, FlinkScalarFeature}
import io.findify.featury.model.Feature.{Counter, ScalarFeature}
import io.findify.featury.model.FeatureConfig.{CounterConfig, ScalarConfig}
import io.findify.featury.model.Write.{Increment, Put}
import io.findify.featury.model._
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.state.KeyedStateStore
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.functions.KeySelector
import org.apache.flink.streaming.api.datastream.DataStreamUtils
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.extensions._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

object FeaturyFlow {
  import io.findify.featury.flink.util.StreamName._

  implicit class WriteDataStream(stream: DataStream[Write]) {

    def process(configs: ConfigMap): DataStream[FeatureValue] = {
      val counters = stream.processCounters(configs.counters)
      val scalars  = stream.processScalar(configs.strings, "scalars", Write.selectPut)
      ???
    }

    def processCounters(configs: Map[FeatureKey, CounterConfig]): DataStream[CounterValue] = {
      stream.processFeature[Increment, CounterConfig, CounterValue, CounterState, Counter](
        "counters",
        configs,
        FlinkCounterFeature.apply,
        Write.selectIncrement
      )
    }

    def processScalar(
        configs: Map[FeatureKey, ScalarConfig],
        name: String,
        select: PartialFunction[Write, Put]
    ): DataStream[ScalarValue] = {
      stream.processFeature[Put, ScalarConfig, ScalarValue, ScalarState, ScalarFeature](
        name = name,
        configs = configs,
        FlinkScalarFeature.apply,
        select
      )
    }

    def processFeature[
        W <: Write: TypeInformation,
        C <: FeatureConfig,
        T <: FeatureValue: TypeInformation,
        S <: State,
        F <: Feature[W, T, C, S]
    ](
        name: String,
        configs: Map[FeatureKey, C],
        make: (KeyedStateStore, C) => F,
        select: PartialFunction[Write, W]
    ): DataStream[T] = {
      stream
        .assignTimestampsAndWatermarks(
          WatermarkStrategy
            .forBoundedOutOfOrderness[Write](java.time.Duration.ofSeconds(1))
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

}
