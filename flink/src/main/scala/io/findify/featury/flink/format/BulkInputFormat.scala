package io.findify.featury.flink.format

import com.github.luben.zstd.ZstdInputStream
import io.findify.featury.flink.util.Compress
import io.findify.featury.model.{State, StateMessage}
import org.apache.flink.api.common.io.FileInputFormat
import org.apache.flink.core.fs.{FileInputSplit, Path}

import java.io.{BufferedInputStream, InputStream}

class BulkInputFormat[T >: Null](path: Path, codec: BulkCodec[T], compress: Compress) extends FileInputFormat[T](path) {
  unsplittable = true
  enumerateNestedFiles = true

  @transient var compressed: InputStream = _
  @transient var next: Option[T]         = None

  override def nextRecord(reuse: T): T = {
    next.orNull
  }

  override def close(): Unit = {
    compressed.close()
  }

  override def reachedEnd(): Boolean = {
    next = codec.read(compressed)
    next.isEmpty
  }

  override def open(fileSplit: FileInputSplit): Unit = {
    val fs = fileSplit.getPath.getFileSystem
    stream = fs.open(fileSplit.getPath, 1024 * 1024)
    compressed = new BufferedInputStream(compress.read(stream), 1024 * 1024)
  }
}
