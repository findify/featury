package io.findify.featury.config

case class Args(configFile: Option[String] = None)

object Args {
  import scopt.OParser
  val builder = OParser.builder[Args]
  val parser = {
    import builder._
    OParser.sequence(
      programName("Featury API"),
      opt[String]("config").action((c, args) => args.copy(configFile = Some(c)))
    )
  }

  def parse(args: List[String]) = {
    val default = Args()
    OParser.parse(parser, args, default).getOrElse(default)
  }
}
