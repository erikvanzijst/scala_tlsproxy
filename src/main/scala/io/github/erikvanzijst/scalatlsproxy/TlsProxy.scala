package io.github.erikvanzijst.scalatlsproxy

import java.nio.channels.Selector

import com.typesafe.scalalogging.StrictLogging

trait KeyHandler {
  def process(): Unit
}


/** Creates a TLS Proxy instance.
  *
  * @param port
  * @param interface  optional local interface address to bind to (e.g. 127.0.0.1, or ::1). Binds to all interface
  *                   when omitted
  */
class TlsProxy(port: Int, interface: Option[String] = None) extends StrictLogging with Runnable with AutoCloseable {
  private var shutdown = false

  override def run(): Unit = {
    val selector = Selector.open
    new ServerHandler(selector, port, interface)

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
