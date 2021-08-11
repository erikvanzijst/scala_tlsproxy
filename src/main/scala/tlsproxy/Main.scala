package tlsproxy

import java.util.concurrent.{ExecutorService, Executors}

import com.typesafe.scalalogging.StrictLogging
import scopt.OptionParser

object Main extends StrictLogging {
  def main(args: Array[String]): Unit = {

    case class Config(port: Int = 3128)

    val parser: OptionParser[Config] = new OptionParser[Config]("tlsproxy") {
      head("tlsproxy", "0.1")
      opt[Int]("port")
        .text("The port to listen on")
        .action((value, cfg) => cfg.copy(port = value))
    }

    implicit val executor: ExecutorService = Executors.newFixedThreadPool(10)
    parser.parse(args, Config()) match {
      case Some(config) =>
        new TlsProxy(config.port).run()
      case None =>
        logger.error("Invalid arguments")
        System.exit(1)
    }
  }

}
