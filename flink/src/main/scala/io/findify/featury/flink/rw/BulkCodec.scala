package io.findify.featury.flink.rw

import io.findify.featury.model.{FeatureKey, FeatureValue, FeatureValueMessage, State, StateMessage}

import java.io.{InputStream, InputStreamReader, OutputStream}
import io.circe.parser._
import io.circe.syntax._

import java.util.Scanner

/** A codec describing how to write and read items of type T from disk. There are multiple predefined formats
  * already available:
  * - BulkCodec.featureValueProtobufCodec: Protobuf format codec for FeatureValue
  * - BulkCodec.stateProtobufCodec: Protobuf codec for internal Featury state, may be used for bootstrapping jobs.
  * - BulkCodec.featureValueJsonCodec: JSON if you need to have something human-readable.
  * @tparam T
  */
trait BulkCodec[T] extends Serializable {

  /** Map item to the bucket in a case if we want partition events into a separate buckets
    * @param value
    * @return
    */
  def bucket(value: T): String

  /** Write item to a stream
    * @param value
    * @param stream
    */
  def write(value: T, stream: OutputStream): Unit

  /** Read event from a stream
    * @param stream
    * @return event or None if there is end-of-file.
    */
  def read(stream: InputStream): Option[T]
}

object BulkCodec {
  lazy val featureValueProtobufCodec = new BulkCodec[FeatureValue] {
    override def read(stream: InputStream): Option[FeatureValue] =
      FeatureValueMessage.parseDelimitedFrom(stream).flatMap(_.toFeatureValue)
    override def write(value: FeatureValue, stream: OutputStream): Unit = value.asMessage.writeDelimitedTo(stream)
    override def bucket(value: FeatureValue): String                    = value.key.fqdn
  }

  lazy val featureValueJsonCodec = new BulkCodec[FeatureValue] {
    import io.findify.featury.model.json.FeatureValueJson._
    override def read(stream: InputStream): Option[FeatureValue] = {
      val scanner = new Scanner(stream)
      val json    = scanner.nextLine()
      decode[FeatureValue](json).toOption
    }

    override def write(value: FeatureValue, stream: OutputStream): Unit = {
      val json = value.asJson.noSpaces
      stream.write(json.getBytes())
    }
    override def bucket(value: FeatureValue): String = value.key.fqdn
  }

  lazy val stateProtobufCodec = new BulkCodec[State] {
    override def bucket(value: State): String                    = value.key.fqdn
    override def write(value: State, stream: OutputStream): Unit = value.asMessage.writeDelimitedTo(stream)
    override def read(stream: InputStream): Option[State]        = StateMessage.parseDelimitedFrom(stream).flatMap(_.toState)
  }

}
