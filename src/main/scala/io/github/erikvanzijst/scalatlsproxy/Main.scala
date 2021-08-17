package io.github.erikvanzijst.scalatlsproxy

import ch.qos.logback.classic.Level
import com.typesafe.scalalogging.StrictLogging
import org.slf4j.LoggerFactory

object Main extends StrictLogging {

  // Suppress debug when running from the shell
  Seq("io.github.erikvanzijst.scalatlsproxy.TlsProxyHandler",
      "io.github.erikvanzijst.scalatlsproxy.ServerHandler",
      "io.github.erikvanzijst.scalatlsproxy.Pipe")
    .map(LoggerFactory.getLogger)
    .map(_.asInstanceOf[ch.qos.logback.classic.Logger])
    .foreach(_.setLevel(Level.INFO))

  def main(args: Array[String]): Unit = new TlsProxy(3128).run()
}
