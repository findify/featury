package io.findify.featury.feature

import io.findify.featury.model.{Feature, Timestamp}
import io.findify.featury.model.Feature.{Op, State}
import io.findify.featury.model.FeatureValue.{BoundedListValue, ListItem, Num, Scalar, Text}
import io.findify.featury.model.Schema.FeatureConfig

import scala.concurrent.duration._

object BoundedList {
  case class Push[T <: Scalar](value: T, ts: Timestamp)               extends Op
  case class BoundedListState[T <: Scalar](values: List[ListItem[T]]) extends State
  case class BoundedListConfig(count: Int = Int.MaxValue, duration: FiniteDuration = Long.MaxValue.nanos)
      extends FeatureConfig

  object TextBoundedListFeature extends BoundedListFeature[Text]
  object NumBoundedListFeature  extends BoundedListFeature[Num]

  class BoundedListFeature[T <: Scalar]
      extends Feature[BoundedListState[T], BoundedListValue[T], Push[T], BoundedListConfig] {
    override def emptyState(conf: BoundedListConfig): BoundedListState[T] = BoundedListState(Nil)
    override def value(conf: BoundedListConfig, state: BoundedListState[T]): BoundedListValue[T] = BoundedListValue(
      state.values
    )
    override def update(conf: BoundedListConfig, state: BoundedListState[T], op: Push[T]): BoundedListState[T] = {
      val start = op.ts.minus(conf.duration)
      BoundedListState((ListItem(op.value, op.ts) :: state.values).take(conf.count).filter(_.ts.isAfter(start)))
    }
  }

}
