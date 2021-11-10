package io.findify.featury.connector.memory

import cats.effect
import cats.effect.IO
import cats.effect.kernel.Resource
import com.github.microwww.redis.RedisServer
import io.findify.featury.values.StoreCodec.ProtobufCodec
import io.findify.featury.values.ValueStoreConfig.RedisConfig

case class OffheapMemoryStore(handle: RedisServer) {
  def config: RedisConfig = RedisConfig(
    host = "localhost",
    port = handle.getSockets.getServerSocket.getLocalPort,
    codec = ProtobufCodec
  )
}

object OffheapMemoryStore {

  def start(port: Int): Resource[IO, OffheapMemoryStore] =
    effect.Resource.make(IO(startUnsafe(port)))(mem => IO(stopUnsafe(mem)))

  def startUnsafe(port: Int) = {
    val server = new RedisServer()
    server.listener("127.0.0.1", port)
    server.getSchema // hack for https://github.com/microwww/redis-mock/pull/6
    OffheapMemoryStore(server)
  }

  def stopUnsafe(store: OffheapMemoryStore) = {
    store.handle.close()
  }
}
