package org.apache.flink

object ScalaJavaDataset {

  /** A hacky way to access java dataset out of scala api.
    * Needed for state processor API which is java-only, but needs to be invoked
    * from the scala code.
    * @param self
    * @tparam T
    */
  implicit class DatasetScalaToJava[T](self: org.apache.flink.api.scala.DataSet[T]) {
    def toJava: org.apache.flink.api.java.DataSet[T] = self.javaSet
  }
}
