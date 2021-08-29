package io.github.erikvanzijst.scalatlsproxy

import com.typesafe.scalalogging.StrictLogging

import java.net.{ServerSocket, Socket, SocketTimeoutException}
import java.util.concurrent.ExecutorService
import scala.collection.mutable
import scala.util.Try

class EchoServer(implicit executor: ExecutorService) extends AutoCloseable with StrictLogging {
  private var shutdown = false
  private val socket = new ServerSocket(0)
  private val connections = mutable.Set[Socket]()
  val port: Int = socket.getLocalPort

  socket.setSoTimeout(100)

  executor.execute(() => try {
    logger.info("EchoServer started on port {}...", port)
    while (!shutdown)
      try {
        val client = socket.accept()
        connections += client
        handle(client)
      } catch {
        case _: SocketTimeoutException =>
      }
  } finally {
    Try(connections.foreach(_.close())).failed.foreach(logger.error("Failed to close connections", _))
    logger.info("EchoServer stopped listening")
  })

  private def handle(client: Socket): Unit = {
    executor.execute(() => try {
      logger.info("Echo client connected from {}", client.getRemoteSocketAddress)
      val bis = client.getInputStream
      val bos = client.getOutputStream

      try {
        Iterator.continually(bis.read)
          .takeWhile(-1 != _)
          .foreach(bos.write)
      } finally {
        connections -= client
        bis.close()
        bos.close()
      }
    } finally {
      logger.info("Echo connection to {} terminated", client.getRemoteSocketAddress)
    })
  }

  override def close(): Unit = shutdown = true
}
