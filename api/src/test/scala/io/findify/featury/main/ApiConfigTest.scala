package io.findify.featury.main

import cats.effect.unsafe.implicits.global
import io.findify.featury.config.ApiConfig
import io.findify.featury.values.StoreCodec.JsonCodec
import io.findify.featury.values.ValueStoreConfig.{MemoryConfig, RedisConfig}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ApiConfigTest extends AnyFlatSpec with Matchers {
  it should "parse mem-only config" in {
    val conf =
      """store:
        |  type: memory""".stripMargin
    ApiConfig.fromString(conf).unsafeRunSync() shouldBe ApiConfig(MemoryConfig())
  }
  it should "parse redis config" in {
    val cfg =
      """store: 
        |  type: redis
        |  host: a
        |  port: 2
        |  codec: json
        |""".stripMargin
    ApiConfig.fromString(cfg).unsafeRunSync() shouldBe ApiConfig(RedisConfig("a", 2, JsonCodec))
  }
}
