package io.findify.featury.main

import io.findify.featury.config.Args
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ArgsTest extends AnyFlatSpec with Matchers {
  it should "parse config file name" in {
    Args.parse(List("--config", "/foo/bar")) shouldBe Args(Some("/foo/bar"))
  }
}
