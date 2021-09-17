package io.findify.featury.flink

import io.findify.featury.flink.FeatureJoinTest.{
  MerchantScope,
  ProductLine,
  ProductScope,
  SearchScope,
  UserScope,
  productJoin
}
import io.findify.featury.model.FeatureConfig.{CounterConfig, ScalarConfig}
import io.findify.featury.model.Key.{FeatureName, Scope, Tag, Tenant}
import io.findify.featury.model.{CounterValue, FeatureValue, SString, ScalarValue, Schema, Timestamp, Write}
import io.findify.featury.model.Write.{Increment, Put}
import io.findify.featury.utils.TestKey
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.findify.flinkadt.api._
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}

import scala.concurrent.duration._

class FeatureJoinTest extends AnyFlatSpec with Matchers with FlinkStreamTest {
  val now = Timestamp.now

  it should "join with latest value" in {
    val session = ProductLine(merchant = "1", search = "q1", product = "p1", user = "u1", now)
    val sessions = env
      .fromCollection(List(session))
      .assignTimestampsAndWatermarks(
        WatermarkStrategy
          .forBoundedOutOfOrderness(java.time.Duration.ofSeconds(10))
          .withTimestampAssigner(new SerializableTimestampAssigner[ProductLine] {
            override def extractTimestamp(element: ProductLine, recordTimestamp: Long): Long = element.ts.ts
          })
      )
    val schema = Schema(
      List(
        ScalarConfig(MerchantScope, FeatureName("lang"), refresh = 0.seconds),
        ScalarConfig(ProductScope, FeatureName("title"), refresh = 0.seconds),
        CounterConfig(SearchScope, FeatureName("count"), refresh = 0.seconds),
        CounterConfig(UserScope, FeatureName("count"), refresh = 0.seconds),
        CounterConfig(ProductScope, FeatureName("clicks"), refresh = 0.seconds)
      )
    )

    val writes = env
      .fromCollection(
        List[Write](
          Put(TestKey(scope = "merchant", fname = "lang", id = "1"), now.minus(2.minute), SString("en")),
          Put(TestKey(scope = "product", fname = "title", id = "p1"), now.minus(10.minute), SString("socks")),
          Put(TestKey(scope = "product", fname = "title", id = "p1"), now.minus(5.minute), SString("xsocks")),
          Increment(TestKey(scope = "search", fname = "count", id = "q1"), now.minus(10.minute), 1),
          Increment(TestKey(scope = "search", fname = "count", id = "q1"), now.minus(9.minute), 1),
          Increment(TestKey(scope = "search", fname = "count", id = "q1"), now.minus(8.minute), 1),
          Increment(TestKey(scope = "product", fname = "clicks", id = "p1"), now.minus(10.minute), 1),
          Increment(TestKey(scope = "product", fname = "clicks", id = "p1"), now.minus(5.minute), 1)
        )
      )

    val features = Featury.process(writes, schema, 10.seconds)

    val joined =
      Featury.join[ProductLine](
        features,
        sessions,
        FeatureJoinTest.productJoin,
        schema
      )

    val result = joined.executeAndCollect(100)
    result.head.values should contain theSameElementsAs List(
      CounterValue(TestKey(scope = "search", fname = "count", id = "q1"), now.minus(8.minute), 3),
      CounterValue(TestKey(scope = "product", fname = "clicks", id = "p1"), now.minus(5.minute), 2L),
      ScalarValue(TestKey(scope = "product", fname = "title", id = "p1"), now.minus(5.minute), SString("xsocks")),
      ScalarValue(TestKey(scope = "merchant", fname = "lang", id = "1"), now.minus(2.minute), SString("en"))
    )

  }

}

object FeatureJoinTest {
  val MerchantScope = Scope("merchant")
  val ProductScope  = Scope("product")
  val SearchScope   = Scope("search")
  val UserScope     = Scope("user")
  case class ProductLine(
      merchant: String,
      search: String,
      product: String,
      user: String,
      ts: Timestamp,
      values: List[FeatureValue] = Nil
  )

  implicit val productJoin = new Join[ProductLine] {
    override def join(self: ProductLine, values: List[FeatureValue]): ProductLine =
      self.copy(values = values ++ self.values)

    override def by(left: ProductLine): Tenant = Tenant(left.merchant)

    override def tags(left: ProductLine): List[Tag] = {
      List(
        Tag(Scope("merchant"), left.merchant),
        Tag(Scope("product"), left.product),
        Tag(Scope("search"), left.search),
        Tag(Scope("user"), left.user)
      )
    }
  }

}
