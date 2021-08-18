package io.github.erikvanzijst.scalatlsproxy

import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, SocketChannel}

import com.typesafe.scalalogging.StrictLogging

class Pipe(fromKey: SelectionKey, fromChannel: SocketChannel, toKey: SelectionKey, toChannel: SocketChannel)
  extends KeyHandler with StrictLogging {

  private val buffer = ByteBuffer.allocate(1 << 16)
  private var shutdown = false
  private var count: Long = 0

  def bytes: Long = count

  def isClosed: Boolean = shutdown && buffer.position() == 0

  override def process(): Unit = {

    if (fromKey.isValid && fromKey.isReadable && !shutdown) {
      val len = fromChannel.read(buffer)
      if (len == -1) {
        logger.debug("{} -> {} EOF reached", fromChannel.getRemoteAddress, toChannel.getRemoteAddress)
        shutdown = true
      } else {
        count = count + len
      }
    }

    if (toKey.isValid && toKey.isWritable && buffer.position() > 0) {
      buffer.flip()
      toChannel.write(buffer)
      buffer.compact()
    }

    if (shutdown && buffer.position() == 0 && toChannel.isOpen) toChannel.shutdownOutput()

    if (toKey.isValid) {
      toKey.interestOps(
        if (buffer.position() > 0) toKey.interestOps() | SelectionKey.OP_WRITE
        else toKey.interestOps() & ~SelectionKey.OP_WRITE)
    }
    if (fromKey.isValid) {
      fromKey.interestOps(
        if (!buffer.hasRemaining || shutdown) fromKey.interestOps() & ~SelectionKey.OP_READ
        else fromKey.interestOps() | SelectionKey.OP_READ)
    }
  }
}
