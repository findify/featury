package io.findify.featury.flink.util

import com.github.luben.zstd.{ZstdInputStream, ZstdOutputStream}
import org.apache.commons.compress.compressors.gzip.{
  GzipCompressorInputStream,
  GzipCompressorOutputStream,
  GzipParameters
}

import java.io.{InputStream, OutputStream}

/** A trait to implement different file compressors for Featury. Right now there are multiple supported out of the box:
  * - NoCompression
  * - ZstdCompression
  * - GzipCompression
  *
  * The trait is not sealed, so you can implement your own.
  */
trait Compress {
  def write(base: OutputStream): OutputStream
  def read(base: InputStream): InputStream
  def ext: String
}

object Compress {
  case object NoCompression extends Compress {
    override def write(base: OutputStream): OutputStream = base
    override def read(base: InputStream): InputStream    = base
    override lazy val ext                                = ""
  }

  case class ZstdCompression(level: Int = 3) extends Compress {
    override def write(base: OutputStream): OutputStream = new ZstdOutputStream(base, level)
    override def read(base: InputStream): InputStream    = new ZstdInputStream(base)
    override lazy val ext                                = ".zst"
  }

  case class GzipCompression(level: Int = 5) extends Compress {
    val params = {
      val p = new GzipParameters()
      p.setCompressionLevel(level)
      p
    }
    override def write(base: OutputStream): OutputStream = new GzipCompressorOutputStream(base, params)
    override def read(base: InputStream): InputStream    = new GzipCompressorInputStream(base)
    override lazy val ext                                = ".gz"
  }
}
