package io.findify.featury.persistence.cassandra

import cats.effect.{IO, Resource}
import io.findify.featury.feature.BoundedList.BoundedListConfig
import io.findify.featury.feature.{BoundedList, BoundedListSuite}
import io.findify.featury.model.FeatureValue
import io.findify.featury.model.FeatureValue.{Num, NumType, Text, TextType}

class CassandraNumBoundedListTest extends BoundedListSuite[Num] with CassandraClient {
  override def contentType: FeatureValue.ScalarType = NumType
  override def makeList(config: BoundedListConfig): Resource[IO, BoundedList[Num]] =
    CassandraPersistence.resource(cassandraConfig).evalMap(_.numBoundedList(config))

  override def makeValue(i: Int): Num = Num(i.toDouble)

}
