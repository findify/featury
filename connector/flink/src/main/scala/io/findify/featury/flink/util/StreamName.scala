package io.findify.featury.flink.util

import org.apache.flink.streaming.api.scala.DataStream

object StreamName {
  implicit class NamedStream[T](self: DataStream[T]) {
    def id(name: String) = self.uid(name).name(name)
  }
}
