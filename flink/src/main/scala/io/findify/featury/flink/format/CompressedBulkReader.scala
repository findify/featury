package io.findify.featury.flink.format

import io.findify.featury.flink.util.Compress
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.file.src.FileSource
import org.apache.flink.connector.file.src.reader.StreamFormat
import org.apache.flink.core.fs.{FSDataInputStream, Path}

import java.io.{BufferedInputStream, InputStream}

object CompressedBulkReader {
  val READ_BUFFER_SIZE = 1024 * 1024

  def readFile[T >: Null](path: Path, compress: Compress, codec: BulkCodec[T])(implicit ti: TypeInformation[T]) = {
    FileSource
      .forRecordStreamFormat[T](CompressedStreamFormat(ti, compress, codec), path)
      .processStaticFileSet()
      .build()
  }

  case class CompressedStreamFormat[T >: Null](ti: TypeInformation[T], compress: Compress, codec: BulkCodec[T])
      extends StreamFormat[T] {
    override def getProducedType: TypeInformation[T] = ti

    override def createReader(
        config: Configuration,
        stream: FSDataInputStream,
        fileLen: Long,
        splitEnd: Long
    ): StreamFormat.Reader[T] = CompressedStreamFormatReader(compress.read(stream), codec)

    override def restoreReader(
        config: Configuration,
        stream: FSDataInputStream,
        restoredOffset: Long,
        fileLen: Long,
        splitEnd: Long
    ): StreamFormat.Reader[T] = {
      val compStream = new BufferedInputStream(compress.read(stream), READ_BUFFER_SIZE)
      compStream.skip(restoredOffset)
      CompressedStreamFormatReader(compStream, codec)
    }

    override def isSplittable: Boolean = false
  }
  case class CompressedStreamFormatReader[T >: Null](
      stream: InputStream,
      codec: BulkCodec[T]
  ) extends StreamFormat.Reader[T] {
    override def read(): T     = codec.read(stream).orNull
    override def close(): Unit = stream.close()
  }

}
