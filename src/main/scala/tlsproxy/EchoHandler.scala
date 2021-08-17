package tlsproxy

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, Selector, SocketChannel}

import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._

class EchoHandler(selector: Selector, socketChannel: SocketChannel) extends KeyHandler with StrictLogging {
  socketChannel.configureBlocking(false)
  private val peer = socketChannel.getRemoteAddress
  private val buffer = ByteBuffer.allocate((1 << 16) - 1)
  private val selectionKey = socketChannel.register(selector, SelectionKey.OP_READ, this)
  private var shutdown = false

  override def process(): Unit = {

    try {
      if (selectionKey.isValid && selectionKey.isReadable) {
        val len = socketChannel.read(buffer)
        if (len == -1) shutdown = true
      }

      if (selectionKey.isValid && selectionKey.isWritable) {
        buffer.flip()
        socketChannel.write(buffer)
        buffer.compact()
      }

      if (shutdown && buffer.position() == 0) {
        close()

      } else if (selectionKey.isValid) {
        var ops = 0
        if (buffer.position() > 0) {
          ops |= SelectionKey.OP_WRITE
        }
        if (buffer.hasRemaining) {
          ops |= SelectionKey.OP_READ
          if ((selectionKey.interestOps & SelectionKey.OP_READ) == 0) logger.info("{} continue reading...", peer)
        } else if ((selectionKey.interestOps & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
          logger.info("{} send buffer full, stop reading...", peer)
        }

        selectionKey.interestOps(ops)
      }
    } catch {
      case e: IOException =>
        logger.error(s"$peer error; connection closed", e)
        close()
    }
  }

  def close(): Unit =
    shutdown = true
    if (selectionKey.isValid) {
      selectionKey.cancel()
      socketChannel.close()
      logger.info("{} connection closed (total connected clients: {})",
        peer, selector.keys().asScala.count(_.attachment().isInstanceOf[EchoHandler]) - 1)
    }
}
