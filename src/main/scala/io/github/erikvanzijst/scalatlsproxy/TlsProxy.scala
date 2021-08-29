package io.github.erikvanzijst.scalatlsproxy

import java.nio.channels.{Selector, SocketChannel}
import com.typesafe.scalalogging.StrictLogging

import java.net.SocketAddress
import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.collection.mutable
import scala.concurrent.duration.Duration

trait KeyHandler {
  def process(): Unit
}

/** Proxy server configuration.
  *
  * @param maxConnections the maximum number of connected clients allowed (0 for unlimited).
  * @param acceptFilter   function taking the address of a new client and returning a boolean to accept or reject it.
  * @param forwardFilter  function taking the client SocketAddress and upstream destination (host, port) and returning
  *                       a boolean to allow or reject it.
  */
case class Config(maxConnections: Int = 0,
                  acceptFilter: SocketAddress => Boolean = _ => true,
                  forwardFilter: (SocketAddress, (String, Int)) => Boolean = (_, _) => true)

/** Creates a TLS Proxy instance.
  *
  * @param port
  * @param interface  optional local interface address to bind to (e.g. "127.0.0.1", or "::1"). Binds to all interface
  *                   when omitted
  */
class TlsProxy(port: Int, interface: Option[String] = None, config: Config = Config())
  extends StrictLogging with Runnable with AutoCloseable {

  private val connections = mutable.Set[TlsProxyHandler]()
  private val selector = Selector.open
  private val latch = new CountDownLatch(1)

  private val server: ServerHandler = new ServerHandler(selector, port, interface) {
    override def onNewConnection(channel: SocketChannel): Unit = {
      val acceptable = config.acceptFilter(channel.getRemoteAddress)

      if (!acceptable || (config.maxConnections > 0 && connections.size >= config.maxConnections)) {
        logger.info(s"${channel.getRemoteAddress} rejected: " +
          (if (!acceptable) "connection not allowed" else s"max connections reached: ${config.maxConnections}"))
        channel.close()

      } else {
        connections += new TlsProxyHandler(selector, channel, config) {
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

  private var shutdown = false
  private var force = false

  override def run(): Unit = try {

    while (!server.isClosed || connections.nonEmpty) {
      if (selector.select(100) > 0) {
        val it = selector.selectedKeys().iterator()
        while (it.hasNext) {
          val key = it.next()
          key.attachment().asInstanceOf[KeyHandler].process()
          it.remove()
        }
      }

      if (shutdown) {
        server.close()
        if (force && connections.nonEmpty) {
          logger.debug("Force-closing {} connections...", connections.size)
          connections.foreach(_.close())
        }
      }
    }
    logger.info("Shutdown successful")

  } finally {
    latch.countDown()
  }

  def getConnectionCount: Int = connections.size

  /** Schedule graceful shutdown of the proxy. Waits for all established connections to close.
    *
    * This method does not block. Actual shutdown occurs asynchronously. Use [[awaitShutdown]] to
    * wait for all resources to get closed.
    *
    * Since proxy connections are often kept alive, there is no guarantee if or when the proxy
    * actually terminates.
    */
  def close(): Unit = shutdown = true

  /** Shut down the proxy and actively close any established connections.
    *
    * This method does not block. Actual shutdown occurs asynchronously. Use [[awaitShutdown]] to
    * wait for all resources to get closed.
    */
  def closeNow(): Unit = if (!shutdown) {
    shutdown = true
    force = true
  }

  def awaitShutdown(timeout: Duration): Boolean = latch.await(timeout.toMillis, TimeUnit.MILLISECONDS)
}
