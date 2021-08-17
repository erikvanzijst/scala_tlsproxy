package tlsproxy

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, Selector, SocketChannel, UnresolvedAddressException}
import java.nio.charset.StandardCharsets

import com.typesafe.scalalogging.StrictLogging
import tlsproxy.TlsProxyHandler.userAgent

import scala.collection.JavaConverters._
import scala.util.Try

object ProxyPhase extends Enumeration {
  type ProxyPhase = Value
  val Destination, Headers, Response, Connecting, Established, Error = Value
}

object TlsProxyHandler {
  private val destPattern = "CONNECT ([^:]+):([0-9]+) HTTP/1.1".r
  private val userAgent = "TlsProxy/1.0 (github.com/erikvanzijst/scala_tlsproxy)"
}

class TlsProxyHandler(selector: Selector, clientChannel: SocketChannel) extends KeyHandler with StrictLogging {
  import ProxyPhase._

  clientChannel.configureBlocking(false)
  private val peer = clientChannel.getRemoteAddress

  private val clientKey = clientChannel.register(selector, SelectionKey.OP_READ, this)  // client initiating the connection
  private val clientBuffer = ByteBuffer.allocate(1 << 16) // client-to-server

  private var serverKey: SelectionKey = _   // the upstream server
  private var serverChannel: SocketChannel = _
  private val serverBuffer = ByteBuffer.allocate(1 << 16) // server-to-client

  private var upstreamPipe: Pipe = _
  private var downstreamPipe: Pipe = _

  private var shutdown = false
  private var destination: (String, Int) = _

  private var phase = Destination

  private def readClient(): Unit = {
    if (clientKey.isValid && clientKey.isReadable && clientChannel.read(clientBuffer) == -1)
        throw new IOException(s"$peer unexpected EOF from client")
    if (!clientBuffer.hasRemaining)
      throw new IOException(s"$peer handshake overflow")
  }


  private def readLine(): Option[String] = {
    readClient()
    clientBuffer.flip()

    val s = StandardCharsets.US_ASCII.decode(clientBuffer).toString
    s.indexOf("\r\n") match {
      case eol if eol != -1 =>
        clientBuffer.position(eol + 2)
        clientBuffer.compact()
        Some(s.substring(0, eol))
      case _ =>
        clientBuffer.position(0)
        clientBuffer.compact()
        None
    }
  }

  private def startResponse(statusCode: Int, statusLine: String, body: String): Unit = {
    val resp = response(statusCode, statusLine, body)
    serverBuffer.put(resp, 0, resp.length)
    clientKey.interestOps(SelectionKey.OP_WRITE)
  }

  private def response(statusCode: Int, statusLine: String, body: String): Array[Byte] =
    (s"HTTP/1.1 $statusCode $statusLine\r\n" +
      s"Proxy-Agent: $userAgent\r\n" +
      "Content-Type: text/plain; charset=us-ascii\r\n" +
      s"Content-Length: ${body.length}\r\n" +
      "\r\n" +
      body).getBytes(StandardCharsets.US_ASCII)

  override def process(): Unit = {
    try {

      if (phase == Destination)
        readLine().map(TlsProxyHandler.destPattern.findFirstMatchIn(_)).foreach {
          case Some(m) =>
            destination = (m.group(1), m.group(2).toInt)
            logger.debug("{} wants to connect to {}:{}...", peer, destination._1, destination._2)
            phase = Headers
          case _ => throw new IOException(s"Malformed request")
        }

      if (phase == Headers)
        Iterator.continually(readLine()).takeWhile(_.isDefined).flatten.foreach {
          case header if header == "" =>
            logger.debug("{} all headers consumed, initiating upstream connection...", peer)

            serverChannel = SocketChannel.open()
            serverChannel.configureBlocking(false)
            serverKey = serverChannel.register(selector, SelectionKey.OP_CONNECT, this)
            clientKey.interestOps(0)  // stop reading while we connect upstream or server a response

            phase = Try {
              if (serverChannel.connect(new InetSocketAddress(destination._1, destination._2))) {
                startResponse(200, "Connection Accepted", "")
                Response
              } else {
                Connecting
              }
            }.recover {
              case _: UnresolvedAddressException =>
                logger.info(s"{} cannot resolve {}", peer, destination._1)
                startResponse(502, "Bad Gateway", s"Failed to resolve ${destination._1}")
                Error
              case iae: IllegalArgumentException =>
                startResponse(400, "Bad Request", s"${iae.getMessage}\n")
                Error
            }.get

          case header => logger.debug("{} ignoring header {}", peer, header)
        }

      if (phase == Connecting)
        if (serverKey.isConnectable)
          phase = Try {
            serverChannel.finishConnect()
            startResponse(200, "Connection Accepted", "")
            Response
          }.recover { case ioe: IOException =>
            startResponse(502, "Gateway Error", s"${ioe.getMessage}\n")
            Error
          }.get

      if (phase == Response)
        if (clientKey.isWritable) {
          serverBuffer.flip()
          clientChannel.write(serverBuffer)
          serverBuffer.compact()

          if (serverBuffer.position() == 0) {
            clientKey.interestOps(SelectionKey.OP_READ)
            serverKey.interestOps(SelectionKey.OP_READ)

            upstreamPipe = new Pipe(clientKey, clientChannel, serverKey, serverChannel)
            downstreamPipe = new Pipe(serverKey, serverChannel, clientKey, clientChannel)

            logger.debug("{} 200 OK sent to client -- TLS connection to {} ready", peer, serverChannel.getRemoteAddress)
            phase = Established
          }
        }

      if (phase == Established) {
        upstreamPipe.process()
        downstreamPipe.process()
        if (upstreamPipe.isClosed && downstreamPipe.isClosed) {
          logger.info("{} -> {} finished (up: {}, down: {})",
            peer, serverChannel.getRemoteAddress, upstreamPipe.bytes, downstreamPipe.bytes)
          close()
        }
      }

      if (phase == Error)
        if (clientKey.isWritable) {
          serverBuffer.flip()
          clientChannel.write(serverBuffer)
          serverBuffer.compact()

          if (serverBuffer.position() == 0) {
            close()
          }
        }

    } catch {
      case e: IOException =>
        logger.error(s"$peer -> ${Option(destination).map(s => s"${s._1}:${s._2}").getOrElse("unconnected")}: error: connection closed: ${e.getClass.getName}: ${e.getMessage}")
        close()
    }
  }

  def close(): Unit = {
    shutdown = true
    if (clientKey.isValid) {
      clientKey.cancel()
      clientChannel.close()
    }
    if (serverKey != null && serverKey.isValid) {
      serverKey.cancel()
      serverChannel.close()
    }
    logger.debug("{} connection closed (total connected clients: {})",
      peer, selector.keys().asScala.count(_.attachment().isInstanceOf[TlsProxyHandler]) - 1)
  }
}
