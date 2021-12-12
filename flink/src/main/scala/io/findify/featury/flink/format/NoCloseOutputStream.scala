package io.findify.featury.flink.format

import java.io.{FilterOutputStream, OutputStream}

class NoCloseOutputStream(base: OutputStream) extends FilterOutputStream(base) {
  override def write(b: Array[Byte]): Unit                     = base.write(b)
  override def write(b: Array[Byte], off: Int, len: Int): Unit = base.write(b, off, len)
  override def close(): Unit = {
    // nope
  }
}
