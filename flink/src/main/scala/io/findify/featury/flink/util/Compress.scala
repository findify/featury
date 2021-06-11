package io.findify.featury.flink.util

import com.github.luben.zstd.{ZstdInputStream, ZstdOutputStream}

import java.io.{InputStream, OutputStream}

sealed trait Compress {
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

  case class ZstdCompression(level: Int) extends Compress {
    override def write(base: OutputStream): OutputStream = new ZstdOutputStream(base, level)
    override def read(base: InputStream): InputStream    = new ZstdInputStream(base)
    override lazy val ext                                = ".zst"
  }
}
