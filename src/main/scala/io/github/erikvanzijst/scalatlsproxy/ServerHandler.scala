package io.github.erikvanzijst.scalatlsproxy

import java.net.InetSocketAddress
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel}

import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._

/** Creates a new server.
  *
  * @param interface  optional local interface address to bind to (e.g. 127.0.0.1, or ::1). Binds to all interface
  *                   when omitted
  */
class ServerHandler(selector: Selector, port: Int, interface: Option[String] = None) extends KeyHandler with StrictLogging {
  private val serverSocketChannel = ServerSocketChannel.open
  serverSocketChannel.socket.bind(interface.map(new InetSocketAddress(_, port)).getOrElse(new InetSocketAddress(port)))
  serverSocketChannel.configureBlocking(false)

  private val serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, this)
  logger.info("Listening on {}...", serverSocketChannel.getLocalAddress)

  override def process(): Unit = {
    val channel = serverSocketChannel.accept()
    new TlsProxyHandler(selector, channel)
    logger.debug("New incoming connection from {} (total connected clients: {})",
      channel.getRemoteAddress, selector.keys().asScala.count(_.attachment().isInstanceOf[TlsProxyHandler]))
  }
}
