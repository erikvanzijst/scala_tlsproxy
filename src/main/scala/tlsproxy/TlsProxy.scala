package tlsproxy

import java.nio.channels.Selector

import ch.qos.logback.classic.Level
import com.typesafe.scalalogging.StrictLogging
import org.slf4j.LoggerFactory

trait KeyHandler {
  def process(): Unit
}


/** Creates a TLS Proxy instance.
  *
  * @param port
  */
class TlsProxy(port: Int) extends StrictLogging with Runnable with AutoCloseable {
  private var shutdown = false

  override def run(): Unit = {
    val selector = Selector.open
    new ServerHandler(selector, port)

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

  override def close(): Unit = {
    shutdown = true
  }
}
