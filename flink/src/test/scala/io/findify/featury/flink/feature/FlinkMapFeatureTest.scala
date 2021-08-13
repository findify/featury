package io.findify.featury.flink.feature

import io.findify.featury.features.MapFeatureSuite
import io.findify.featury.flink.{Featury, FlinkStreamTest}
import io.findify.featury.model.{FeatureValue, Schema, Write}
import io.findify.featury.model.Write.PutTuple
import io.findify.flinkadt.api._

import scala.concurrent.duration._

class FlinkMapFeatureTest extends MapFeatureSuite with FlinkStreamTest {
  override def write(values: List[PutTuple]): Option[FeatureValue] = {
    val conf = Schema(config.copy(refresh = 0.hour))
    Featury.process(env.fromCollection[Write](values), conf, 10.seconds).executeAndCollect(100).lastOption
  }

}
