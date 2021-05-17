package io.findify.featury.service

import cats.effect.IO
import io.findify.featury.feature.{BoundedList, Counter, Feature}
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.WriteRequest.{WriteAction, WriteCounter, WriteNumList, WriteTextList}
import cats.implicits._
import io.findify.featury.feature.BoundedList.BoundedListConfig
import io.findify.featury.feature.Counter.CounterConfig
import io.findify.featury.model.FeatureValue.{Num, NumType, Text, TextType}
import io.findify.featury.model.Schema
import io.findify.featury.model.Schema.FeatureConfig
import io.findify.featury.persistence.Persistence
import io.findify.featury.service.FeatureService.FeatureKey
import shapeless.syntax.typeable._

trait FeatureService[W <: WriteAction, F <: Feature[_, _, _]] {
  def mapping: Map[FeatureKey, F]
  def write(action: W, feature: F): IO[Unit]
  def select(action: WriteAction): Option[W]
  def write(actions: List[WriteAction]): IO[Unit] = {
    val results = for {
      action   <- actions
      selected <- select(action)
      feature  <- mapping.get(FeatureKey(action.key.ns, action.key.group, action.key.featureName))
    } yield {
      write(selected, feature)
    }
    results.sequence.map(_ => IO.unit)
  }
}

object FeatureService {
  case class FeatureKey(ns: Namespace, group: GroupName, feature: FeatureName)

  case class CounterService(schema: Schema, store: Persistence) extends FeatureService[WriteCounter, Counter] {
    override lazy val mapping: Map[FeatureKey, Counter] = load(schema) { case conf: CounterConfig =>
      store.counter(conf)
    }
    override def write(action: WriteCounter, feature: Counter): IO[Unit] = feature.increment(action.key, action.inc)
    override def select(action: WriteAction): Option[WriteCounter]       = action.cast[WriteCounter]
  }

  case class TextBoundedListService(schema: Schema, store: Persistence)
      extends FeatureService[WriteTextList, BoundedList[Text]] {
    override lazy val mapping: Map[FeatureKey, BoundedList[Text]] = load(schema) {
      case conf @ BoundedListConfig(_, _, _, TextType) => store.textBoundedList(conf)
    }
    override def write(action: WriteTextList, feature: BoundedList[Text]): IO[Unit] =
      feature.put(action.key, Text(action.value), action.ts)

    override def select(action: WriteAction): Option[WriteTextList] = action.cast[WriteTextList]
  }

  case class NumBoundedListService(schema: Schema, store: Persistence)
      extends FeatureService[WriteNumList, BoundedList[Num]] {
    override lazy val mapping: Map[FeatureKey, BoundedList[Num]] = load(schema) {
      case conf @ BoundedListConfig(_, _, _, NumType) => store.numBoundedList(conf)
    }
    override def write(action: WriteNumList, feature: BoundedList[Num]): IO[Unit] =
      feature.put(action.key, Num(action.value), action.ts)

    override def select(action: WriteAction): Option[WriteNumList] = action.cast[WriteNumList]
  }

  def load[F <: Feature[_, _, _]](schema: Schema)(pf: PartialFunction[FeatureConfig, F]): Map[FeatureKey, F] = {
    val result = for {
      ns          <- schema.namespaces
      group       <- ns.groups
      featureConf <- group.features
      feature     <- pf.lift(featureConf)
    } yield {
      FeatureKey(ns.name, group.name, featureConf.name) -> feature
    }
    result.toMap
  }

}
