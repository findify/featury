package io.findify.featury.flink

import io.findify.featury.flink.feature.{FlinkCounterFeature, FlinkScalarFeature}
import io.findify.featury.model.Feature.{Counter, ScalarFeature}
import io.findify.featury.model.FeatureConfig.{CounterConfig, ScalarConfig}
import io.findify.featury.model.Write.{Increment, Put}
import io.findify.featury.model._
import org.apache.flink.api.common.eventtime.WatermarkStrategy
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
      val strings =
        stream.processScalar[SString](configs.strings, "strings", Write.selectString, FlinkScalarFeature.applyString)
      val doubles =
        stream.processScalar[SDouble](configs.doubles, "doubles", Write.selectDouble, FlinkScalarFeature.applyDouble)
      ???
    }

    def processCounters(configs: Map[FeatureKey, CounterConfig]): DataStream[LongScalarValue] = {
      stream.processFeature[Increment, CounterConfig, LongScalarValue, Counter](
        "counters",
        configs,
        FlinkCounterFeature.apply,
        Write.selectIncrement
      )
    }

    def processScalar[T <: Scalar](
        configs: Map[FeatureKey, ScalarConfig],
        name: String,
        select: PartialFunction[Write, Put[T]],
        make: (KeyedStateStore, ScalarConfig) => ScalarFeature[T]
    )(implicit
        ti: TypeInformation[T]
    ): DataStream[ScalarValue[T]] = {
      stream.processFeature[Put[T], ScalarConfig, ScalarValue[T], ScalarFeature[T]](
        name = name,
        configs = configs,
        make,
        select
      )
    }

    def processFeature[
        W <: Write: TypeInformation,
        C <: FeatureConfig,
        T <: FeatureValue: TypeInformation,
        F <: Feature[W, T, C]
    ](
        name: String,
        configs: Map[FeatureKey, C],
        make: (KeyedStateStore, C) => F,
        select: PartialFunction[Write, W]
    ): DataStream[T] = {
      stream
        .flatMapWith(w => select.lift(w))
        .id(s"select-${name}")
        .keyingBy(_.key)
        .process(
          new FeatureProcessFunction[W, T, C, F](
            configs = configs,
            name = name,
            make = make
          )
        )
        .id(s"process-${name}")
    }
  }

}
