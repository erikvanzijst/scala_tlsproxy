package io.github.erikvanzijst.scalatlsproxy

import java.nio.channels.Selector
import com.typesafe.scalalogging.StrictLogging

import java.net.SocketAddress

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

  private var shutdown = false

  override def run(): Unit = {
    val selector = Selector.open
    new ServerHandler(selector, port, interface, config)

    while (true) {
      if (selector.select(5000) > 0) {
        val it = selector.selectedKeys().iterator()
        while (it.hasNext) {
          val key = it.next()
          key.attachment().asInstanceOf[KeyHandler].process()
          it.remove()
        }
      }
    }
  }

  override def close(): Unit = shutdown = true
}
