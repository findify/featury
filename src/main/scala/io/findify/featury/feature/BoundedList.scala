package io.findify.featury.feature

import cats.effect.IO
import io.findify.featury.feature.BoundedList.{BoundedListConfig, BoundedListState}
import io.findify.featury.feature.Feature.State
import io.findify.featury.model.{Key, Timestamp}
import io.findify.featury.model.FeatureValue.{BoundedListValue, ListItem, Num, Scalar, ScalarType, Text}
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.Schema.FeatureConfig

import scala.concurrent.duration._

trait BoundedList[T <: Scalar] extends Feature[BoundedListState[T], BoundedListValue[T], BoundedListConfig] {
  def config: BoundedListConfig
  def fromItems(list: List[ListItem[T]]): BoundedListValue[T]
  def put(key: Key, value: T, ts: Timestamp): IO[Unit]
  override def empty(): BoundedListState[T] = BoundedListState(Nil)
  override def computeValue(state: BoundedListState[T]): Option[BoundedListValue[T]] =
    state.values match {
      case Nil => Some(fromItems(Nil))
      case head :: _ =>
        val maxTs      = state.values.map(_.ts).maxBy(_.ts)
        val timeCutoff = maxTs.minus(config.duration)
        Some(fromItems(state.values.filter(_.ts.isAfter(timeCutoff)).take(config.count)))
    }
}

object BoundedList {
  case class BoundedListState[T <: Scalar](values: List[ListItem[T]]) extends State
  case class BoundedListConfig(
      name: FeatureName,
      ns: Namespace,
      group: GroupName,
      count: Int = Int.MaxValue,
      duration: FiniteDuration = Long.MaxValue.nanos,
      contentType: ScalarType
  ) extends FeatureConfig

  trait TextBoundedList extends BoundedList[Text]
  trait NumBoundedList  extends BoundedList[Num]
}
