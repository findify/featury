package io.findify.featury.flink

import io.findify.featury.flink.FeatureJoinTest.ProductLine
import io.findify.featury.flink.util.ScopeKey
import io.findify.featury.model.FeatureConfig.{CounterConfig, ScalarConfig}
import io.findify.featury.model.Key.{FeatureName, GroupName, Namespace}
import io.findify.featury.model.{CounterValue, FeatureValue, SString, ScalarValue, Schema, Timestamp, Write}
import io.findify.featury.model.Write.{Increment, Put}
import io.findify.featury.utils.TestKey
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.findify.flinkadt.api._
import org.apache.flink.api.common.typeinfo.TypeInformation

import scala.concurrent.duration._

class FeatureJoinTest extends AnyFlatSpec with Matchers with FlinkStreamTest {
  val now = Timestamp.now
  import FeatureTypeInfo._

  it should "join with latest value" in {
    val dev      = Namespace("dev")
    val product  = GroupName("product")
    val search   = GroupName("search")
    val merchant = GroupName("merchant")
    val session  = ProductLine(1, "q1", "p1", now)
    val sessions = env.fromCollection(List(session)).assignAscendingTimestamps(_.ts.ts)
    sessions.executeAndCollect(10)
    val schema = Schema(
      List(
        ScalarConfig(dev, merchant, FeatureName("lang"), refresh = 0.seconds),
        ScalarConfig(dev, product, FeatureName("title"), refresh = 0.seconds),
        CounterConfig(dev, search, FeatureName("count"), refresh = 0.seconds),
        CounterConfig(dev, product, FeatureName("clicks"), refresh = 0.seconds)
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

    val features = FeaturyFlow.process(writes, schema)

    val withProduct = FeaturyFlow.join[ProductLine](
      values = features,
      events = sessions,
      scope = p => ScopeKey.make("dev", "product", 1, p.product),
      add = (p, a) => p.copy(values = a ++ p.values)
    )

    val withMerchant = FeaturyFlow.join[ProductLine](
      values = features,
      events = withProduct,
      scope = p => ScopeKey.make("dev", "merchant", 1, p.merchant.toString),
      add = (p, a) => p.copy(values = a ++ p.values)
    )
    val withSearch = FeaturyFlow.join[ProductLine](
      values = features,
      events = withMerchant,
      scope = p => ScopeKey.make("dev", "search", 1, p.search),
      add = (p, a) => p.copy(values = a ++ p.values)
    )

    val result = withSearch.executeAndCollect(100)
    result.headOption shouldBe Some(
      session.copy(values =
        List(
          CounterValue(TestKey(group = "search", fname = "count", id = "q1"), now.minus(8.minute), 3),
          ScalarValue(TestKey(group = "merchant", fname = "lang", id = "1"), now.minus(2.minute), SString("en")),
          CounterValue(TestKey(group = "product", fname = "clicks", id = "p1"), now.minus(5.minute), 2L),
          ScalarValue(TestKey(group = "product", fname = "title", id = "p1"), now.minus(5.minute), SString("xsocks"))
        )
      )
    )
  }

}

object FeatureJoinTest {
  case class ProductLine(
      merchant: Int,
      search: String,
      product: String,
      ts: Timestamp,
      values: List[FeatureValue] = Nil
  )

  sealed trait ADT
  case class Foo(a: Int)       extends ADT
  case class Bar(a: List[Int]) extends ADT

}
