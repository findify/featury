package io.findify.featury.persistence.cassandra

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{ScalarFeature, ScalarFeatureSuite}
import io.findify.featury.model.FeatureValue.{Num, Text}

import scala.util.Random

class CassandraNumScalarTest extends ScalarFeatureSuite[Num] with CassandraClient {
  override def makeValue: Num = Num(Random.nextInt(100000).toDouble)
  override def makeCounter(): Resource[IO, ScalarFeature[Num]] =
    CassandraPersistence.resource(cassandraConfig).evalMap(_.numScalar(config))
}
