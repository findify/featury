package io.findify.featury.flink.feature

import io.findify.featury.features.BoundedListSuite
import io.findify.featury.flink.{Featury, FlinkStreamTest}
import io.findify.featury.flink.FlinkStreamTest
import io.findify.featury.model.Key.{Tag, Tenant}
import io.findify.featury.model.Write.Append
import io.findify.featury.model.{FeatureValue, Key, Schema, Write}
import io.findify.flinkadt.api._

import scala.concurrent.duration._

class FlinkBoundedListTest extends BoundedListSuite with FlinkStreamTest {
  val k = Key(Tag(config.scope, "x1"), config.name, Tenant("1"))

  override def write(values: List[Append]): Option[FeatureValue] = {
    val conf = Schema(List(config.copy(refresh = 0.hour)))
    Featury
      .process(env.fromCollection[Write](values), conf, 10.seconds)
      .executeAndCollect(100)
      .lastOption
  }

}
