package io.findify.featury.persistence.cassandra

import cats.effect.{IO, Resource}
import io.findify.featury.feature.BoundedList.BoundedListConfig
import io.findify.featury.feature.{BoundedList, BoundedListSuite}
import io.findify.featury.model.FeatureValue
import io.findify.featury.model.FeatureValue.{Text, TextType}

class CassandraTextBoundedListTest extends BoundedListSuite[Text] with CassandraClient {
  override def contentType: FeatureValue.ScalarType = TextType
  override def makeList(config: BoundedListConfig): Resource[IO, BoundedList[Text]] =
    CassandraPersistence.resource(cassandraConfig).evalMap(_.textBoundedList(config))

  override def makeValue(i: Int): Text = Text(i.toString)
}
