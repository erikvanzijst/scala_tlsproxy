package tlsproxy

import java.net.InetSocketAddress
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel}

import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._

class ServerHandler(selector: Selector, port: Int) extends KeyHandler with StrictLogging {
  private val serverSocketChannel = ServerSocketChannel.open
  serverSocketChannel.socket.bind(new InetSocketAddress(port))
  serverSocketChannel.configureBlocking(false)

  private val serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, this)
  logger.info("Listening on port {}...", port)

  override def process(): Unit = {
    val channel = serverSocketChannel.accept()
    new ClientHandler(selector, channel)
    logger.info("New incoming connection from {} (total connected clients: {})",
      channel.getRemoteAddress, selector.keys().asScala.count(_.attachment().isInstanceOf[ClientHandler]))
  }
}
