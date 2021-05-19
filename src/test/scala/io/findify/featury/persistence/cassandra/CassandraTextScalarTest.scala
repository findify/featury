package io.findify.featury.persistence.cassandra

import cats.effect.{IO, Resource}
import io.findify.featury.feature.{ScalarFeature, ScalarFeatureSuite}
import io.findify.featury.model.FeatureValue.Text

import scala.util.Random

class CassandraTextScalarTest extends ScalarFeatureSuite[Text] with CassandraClient {
  override def makeValue: Text = Text(Random.nextInt(100000).toString)
  override def makeCounter(): Resource[IO, ScalarFeature[Text]] =
    CassandraPersistence.resource(cassandraConfig).evalMap(_.textScalar(config))
}
