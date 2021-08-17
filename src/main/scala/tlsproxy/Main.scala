package tlsproxy

import java.util.concurrent.{ExecutorService, Executors}

import ch.qos.logback.classic.Level
import com.typesafe.scalalogging.StrictLogging
import org.slf4j.LoggerFactory
import scopt.OptionParser

object Main extends StrictLogging {

  // Suppress debug when running from the shell
  Seq("tlsproxy.TlsProxyHandler", "tlsproxy.ServerHandler", "tlsproxy.Pipe")
    .map(LoggerFactory.getLogger)
    .map(_.asInstanceOf[ch.qos.logback.classic.Logger])
    .foreach(_.setLevel(Level.INFO))

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
