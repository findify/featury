package io.findify.featury.flink

import io.findify.featury.flink.FeatureJoinTest.{MerchantScope, ProductLine, ProductScope, SearchScope, UserScope}
import io.findify.featury.model.FeatureConfig.{CounterConfig, ScalarConfig}
import io.findify.featury.model.Key.{FeatureName, Namespace, Scope}
import io.findify.featury.model.{
  CounterValue,
  FeatureValue,
  SString,
  ScalarValue,
  Schema,
  ScopeKey,
  ScopeKeyOps,
  Timestamp,
  Write
}
import io.findify.featury.model.Write.{Increment, Put}
import io.findify.featury.utils.TestKey
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.flink.api.scala._

import scala.concurrent.duration._

class FeatureJoinTest extends AnyFlatSpec with Matchers with FlinkStreamTest {
  val now = Timestamp.now

  it should "join with latest value" in {
    val dev      = Namespace("dev")
    val session  = ProductLine(merchant = "1", search = "q1", product = "p1", user = "u1", now)
    val sessions = env.fromCollection(List(session)).assignAscendingTimestamps(_.ts.ts)
    sessions.executeAndCollect(10)
    val schema = Schema(
      List(
        ScalarConfig(dev, MerchantScope, FeatureName("lang"), refresh = 0.seconds),
        ScalarConfig(dev, ProductScope, FeatureName("title"), refresh = 0.seconds),
        CounterConfig(dev, SearchScope, FeatureName("count"), refresh = 0.seconds),
        CounterConfig(dev, UserScope, FeatureName("count"), refresh = 0.seconds),
        CounterConfig(dev, ProductScope, FeatureName("clicks"), refresh = 0.seconds)
      )
    )

    val writes = env
      .fromCollection(
        List[Write](
          Put(TestKey(group = "merchant", fname = "lang", id = "1"), now.minus(2.minute), SString("en")),
          Put(TestKey(group = "product", fname = "title", id = "p1"), now.minus(10.minute), SString("socks")),
          Put(TestKey(group = "product", fname = "title", id = "p1"), now.minus(5.minute), SString("xsocks")),
          Increment(TestKey(group = "search", fname = "count", id = "q1"), now.minus(10.minute), 1),
          Increment(TestKey(group = "search", fname = "count", id = "q1"), now.minus(9.minute), 1),
          Increment(TestKey(group = "search", fname = "count", id = "q1"), now.minus(8.minute), 1),
          Increment(TestKey(group = "product", fname = "clicks", id = "p1"), now.minus(10.minute), 1),
          Increment(TestKey(group = "product", fname = "clicks", id = "p1"), now.minus(5.minute), 1)
        )
      )
      .assignAscendingTimestamps(_.ts.ts)

    val features = Featury.process(writes, schema)

    val joined =
      Featury.join[ProductLine](
        features,
        sessions,
        List(MerchantScope, ProductScope, SearchScope, UserScope),
        FeatureJoinTest.productJoin
      )

    val result = joined.executeAndCollect(100)
    result.headOption shouldBe Some(
      session.copy(values =
        List(
          CounterValue(TestKey(group = "search", fname = "count", id = "q1"), now.minus(8.minute), 3),
          CounterValue(TestKey(group = "product", fname = "clicks", id = "p1"), now.minus(5.minute), 2L),
          ScalarValue(TestKey(group = "product", fname = "title", id = "p1"), now.minus(5.minute), SString("xsocks")),
          ScalarValue(TestKey(group = "merchant", fname = "lang", id = "1"), now.minus(2.minute), SString("en"))
        )
      )
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

    override def key(value: ProductLine, scope: Scope): ScopeKey = scope match {
      case MerchantScope => ScopeKey.make("dev", "merchant", "1", value.merchant)
      case ProductScope  => ScopeKey.make("dev", "product", "1", value.product)
      case SearchScope   => ScopeKey.make("dev", "search", "1", value.search)
      case UserScope     => ScopeKey.make("dev", "user", "1", value.search)
      case _             => ???
    }
  }

  sealed trait ADT
  case class Foo(a: Int)       extends ADT
  case class Bar(a: List[Int]) extends ADT

}
