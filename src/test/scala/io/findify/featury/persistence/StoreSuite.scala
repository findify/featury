package io.findify.featury.persistence

import io.findify.featury.feature.Counter.CounterState
import io.findify.featury.model.Feature.State
import io.findify.featury.model.Key
import cats.effect.{IO, Resource}
import org.scalatest.Outcome
import org.scalatest.flatspec.FixtureAnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.unsafe.implicits.global

trait StoreSuite[S <: State, T <: StateStore[S]] extends FixtureAnyFlatSpec with Matchers {
  case class FixtureParam(p: T)
  def makePersistence(): Resource[IO, T]
  override def withFixture(test: OneArgTest): Outcome = {
    val (p, shutdownHandle) = (makePersistence().allocated.unsafeRunSync())
    try {
      withFixture(test.toNoArgTest(FixtureParam(p)))
    } finally {
      shutdownHandle.unsafeRunSync()
    }
  }
  it should "read empty" in { fixture =>
    val result = fixture.p.read(List(Key("a", "b", 1, "c"))).unsafeRunSync()
    result shouldBe Map.empty
  }

}
