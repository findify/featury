package io.findify.featury.flink.util

import org.apache.flink.streaming.api.scala.DataStream

object StreamName {

  /** A helper class to set stream uid (for snapshots) and name (for web ui) at the same time.
    * @param self
    * @tparam T
    */
  implicit class NamedStream[T](self: DataStream[T]) {
    def id(name: String) = self.uid(name).name(name)
  }
}
