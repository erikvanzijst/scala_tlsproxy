package io.github.erikvanzijst.scalatlsproxy

import java.net.InetSocketAddress
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel}
import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._
import scala.collection.mutable

/** Creates a new server.
  *
  * @param interface  optional local interface address to bind to (e.g. "127.0.0.1", or "::1"). Binds to all interface
  *                   when omitted
  */
class ServerHandler(selector: Selector, port: Int, interface: Option[String] = None, config: Config)
  extends KeyHandler with StrictLogging {

  private val connections = mutable.Set[TlsProxyHandler]()
  private val serverSocketChannel = ServerSocketChannel.open

  serverSocketChannel.socket.bind(interface.map(new InetSocketAddress(_, port)).getOrElse(new InetSocketAddress(port)))
  serverSocketChannel.configureBlocking(false)
  serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, this)
  logger.info("Listening on {}...", serverSocketChannel.getLocalAddress)

  override def process(): Unit = {
    val channel = serverSocketChannel.accept()
    if (config.maxConnections > 0 && connections.size >= config.maxConnections) {
      logger.warn("Rejecting connection from {}, max connections reached: {}",
        channel.getRemoteAddress, config.maxConnections)
      channel.close()

    } else {
      connections += new TlsProxyHandler(selector, channel) {
        override def close(): Unit = {
          connections -= this
          super.close()
        }
      }
      logger.debug("New incoming connection from {} (total connected clients: {})",
        channel.getRemoteAddress, connections.size)
    }
  }
}
