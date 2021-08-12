package io.findify.featury.flink

import io.findify.featury.flink.rw.{BulkCodec, CompressedBulkReader, CompressedBulkWriter}
import io.findify.featury.flink.util.Compress
import io.findify.featury.model.Key.Scope
import io.findify.featury.model.{Schema, ScopeKeyOps, _}
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala.extensions._
import org.apache.flink.api.scala._
import org.apache.flink.connector.file.sink.FileSink
import org.apache.flink.connector.file.src.FileSource
import org.apache.flink.core.fs.Path

/** Featury Flink API
  */
object Featury {
  import io.findify.featury.flink.util.StreamName._

  /** Perform a temporal join of user-supplied session information (of type T) with a set of feature values. For example,
    * you have two scopes 'user' and 'item'. For each scope you counting a number of interactions made. Then this function
    * will:
    * - take a stream of session events (like each new interaction made)
    * - for each event it will select the single latest feature value
    * - attach this feature value to the session event
    *
    * In SQL terms, it's more like this:
    *   - select event, value from events inner join values on event.key=value.key where event.ts>=value.ts
    *   - but as for the right part of join we only care about the latest feature value, so we also
    *   drop all stale values.
    *
    * @param values a stream of feature values. See Featury.process for details how to build one.
    * @param events a stream of timestamped session events. Timestamp info is taken from watermark (or event timestamps)
    *               from flink itself.
    * @param scopes a set of scopes to make join over. This function recursively joins each scope one by one.
    * @param by join definition
    * @tparam T
    * @return a stream of original session events, but with corresponsing feature values attached.
    */
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
          .keyBy[Option[ScopeKey]](t => by.key(t, head), t => Some(ScopeKey(t.key)))
          .process(new FeatureJoinFunction[T](by))
          .id(s"join-$head")
        join(values, result, tail, by)

    }

  /** Process a set of source interactions according to the defined feature schema. This function:
    * - for each interaction will update the corresponding feature value
    * - if it's time for a feature value to emit an updated snapshot, it will do it.
    * @param stream stream
    * @param schema
    * @return
    */
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

  /** Write feature values into some path in native format.
    * @param path path to write to
    * @param compress should we compress it?
    * @param codec codec to use for writing the binary data. By default it dumps in protobuf format,
    *              but you can use whatever format you want. See BulkCodec[T] for details.
    * @return
    */
  def writeFeatures(
      path: Path,
      compress: Compress,
      codec: BulkCodec[FeatureValue] = BulkCodec.featureValueProtobufCodec
  ): FileSink[FeatureValue] = CompressedBulkWriter.writeFile(
    path = path,
    compress = compress,
    codec = codec,
    prefix = "values"
  )

  /** Read feature values from disk. It read recursively from the dir.
    * @param path path to read from.
    * @param compress How should we decompress the stream?
    * @param codec which codec to use for parsing
    * @param ti
    * @return
    */
  def readFeatures(
      path: Path,
      compress: Compress,
      codec: BulkCodec[FeatureValue] = BulkCodec.featureValueProtobufCodec
  )(implicit
      ti: TypeInformation[FeatureValue]
  ): FileSource[FeatureValue] =
    CompressedBulkReader.readFile(path, compress, codec)

  /** Write
    * @param path
    * @param compress
    * @return
    */
  def writeState(
      path: Path,
      compress: Compress,
      codec: BulkCodec[State] = BulkCodec.stateProtobufCodec
  ): FileSink[State] =
    CompressedBulkWriter.writeFile(
      path = path,
      compress = compress,
      codec = codec,
      prefix = "state"
    )

  def readState(path: Path, compress: Compress, codec: BulkCodec[State] = BulkCodec.stateProtobufCodec)(implicit
      ti: TypeInformation[State]
  ): FileSource[State] =
    CompressedBulkReader.readFile(path, compress, codec)

}
