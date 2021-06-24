package io.findify.featury.flink

import io.findify.featury.model.Key.Scope
import io.findify.featury.model.{Schema, ScopeKeyOps, _}
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala.extensions._

object FeaturyFlow {
  import io.findify.featury.flink.util.StreamName._

  def join[T](values: DataStream[FeatureValue], events: DataStream[T], scopes: List[Scope], by: Join[T])(implicit
      ti: TypeInformation[T],
      ki: TypeInformation[Option[ScopeKey]],
      si: TypeInformation[String],
      fvi: TypeInformation[FeatureValue]
  ): DataStream[T] =
    scopes match {
      case Nil => events
      case head :: tail =>
        val result = events
          .connect(values)
          .keyBy[Option[ScopeKey]](t => by.scopedKey(t, head), t => Some(ScopeKey(t.key)))
          .process(new FeatureJoinFunction[T](by))
          .id(s"join-${head.value}")
        join(values, result, tail, by)

    }

  def process(stream: DataStream[Write], schema: Schema)(implicit
      ki: TypeInformation[Key],
      fvi: TypeInformation[FeatureValue],
      longTI: TypeInformation[Long],
      intTI: TypeInformation[Int],
      doubleTI: TypeInformation[Double],
      tvTI: TypeInformation[TimeValue],
      stringTI: TypeInformation[String],
      scalarTI: TypeInformation[Scalar],
      stateTI: TypeInformation[State]
  ): DataStream[FeatureValue] = {
    stream
      .assignTimestampsAndWatermarks(
        WatermarkStrategy
          .forMonotonousTimestamps()
          .withTimestampAssigner(new SerializableTimestampAssigner[Write] {
            override def extractTimestamp(element: Write, recordTimestamp: Long): Long = element.ts.ts
          })
      )
      .keyingBy(_.key)
      .process(new FeatureProcessFunction(schema))
      .id("feature-process")
  }
}
