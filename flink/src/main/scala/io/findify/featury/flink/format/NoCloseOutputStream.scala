package io.findify.featury.flink.format

import java.io.{FilterOutputStream, OutputStream}

class NoCloseOutputStream(base: OutputStream) extends FilterOutputStream(base) {
  override def close(): Unit = {
    // nope
  }
}
