package io.github.erikvanzijst.scalatlsproxy

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import java.net.InetSocketAddress
import java.nio.channels.{ClosedChannelException, SelectionKey, Selector, ServerSocketChannel, SocketChannel}

/** Creates a new server.
  *
  * @param interface  optional local interface address to bind to (e.g. "127.0.0.1", or "::1"). Binds to all interface
  *                   when omitted
  */
abstract class ServerHandler(selector: Selector, port: Int, interface: Option[String] = None)
  extends KeyHandler with AutoCloseable {

  protected val logger: Logger = Logger(LoggerFactory.getLogger("io.github.erikvanzijst.scalatlsproxy.ServerHandler"))
  private val serverSocketChannel = ServerSocketChannel.open
  serverSocketChannel.socket.bind(interface.map(new InetSocketAddress(_, port)).getOrElse(new InetSocketAddress(port)))
  serverSocketChannel.configureBlocking(false)
  private val serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, this)
  @volatile private var shutdown = false

  logger.info("Listening on {}...", serverSocketChannel.getLocalAddress)

  override def process(): Unit =
    try {
      onNewConnection(serverSocketChannel.accept())
    } catch {
      case _: ClosedChannelException if shutdown => // benign race condition with [[close()]]
    }

  def onNewConnection(channel: SocketChannel): Unit

  def close(): Unit = {
    shutdown = true
    if (serverKey.isValid) serverKey.cancel()
    if (serverSocketChannel.isOpen) {
      serverSocketChannel.close()
      logger.info("Stopped listening")
    }
  }

  def isClosed: Boolean = !serverSocketChannel.isOpen
}
