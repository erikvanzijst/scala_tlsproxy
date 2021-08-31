package io.github.erikvanzijst.scalatlsproxy

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter, Writer}
import java.net.{ServerSocket, Socket}
import java.nio.charset.StandardCharsets
import java.util.concurrent.{ExecutorService, Executors, TimeUnit}
import scala.concurrent.duration._
import resource._

class TlsProxyTest extends AnyFunSuite with BeforeAndAfter with StrictLogging {
  private implicit var executor: ExecutorService = _
  private val connectPattern = "^HTTP/1.1 200 .+".r
  private var proxyPort: Int = _

  before {
    executor = Executors.newFixedThreadPool(10)
    proxyPort = getAvailablePort
  }

  test("Happy path") {

    for {
      proxy <- managed(new TlsProxy(proxyPort))
      server <- managed(new EchoServer())
    } {
      executor.execute(proxy)
      Thread.sleep(100)

      val (client, writer, reader) = proxyConnect("localhost", server.port)
      for (_ <- managed(client)) {
        writer.write("Hello\n")
        writer.flush()
        assert(reader.readLine() == "Hello")
      }
    }
  }

  test("AcceptFilter blocks properly") {
    for (proxy <- managed(new TlsProxy(proxyPort, config = Config(acceptFilter = _ => false)))) {
      executor.execute(proxy)
      Thread.sleep(100)

      for (client <- managed(new Socket("localhost", proxyPort))) { // should get accepted...
        assert(client.getInputStream.read() == -1) // ...and immediately closed
      }
    }
  }

  test("Max connections enforced") {
    for {
      proxy <- managed(new TlsProxy(proxyPort, config = Config(maxConnections = 1)))
      server <- managed(new EchoServer())
    } {
      executor.execute(proxy)
      Thread.sleep(100)

      val (client1, writer1, reader1) = proxyConnect("localhost", server.port)
      for {
        _ <- managed(client1)
        client2 <- managed(new Socket("localhost", proxyPort))
      } {
        // client2 should get accepted and immediately closed
        assert(client2.getInputStream.read() == -1, "Connection should have been closed")

        // assert client1 still works:
        writer1.write("Hello\n")
        writer1.flush()
        assert(reader1.readLine() == "Hello")
      }
    }
  }

  test("closeNow() forcefully disconnects clients") {
    for {
      server <- managed(new EchoServer())
    } {
      val proxy = new TlsProxy(proxyPort, config = Config(maxConnections = 1))
      executor.execute(proxy)
      Thread.sleep(100)

      val (_, _, reader) = proxyConnect("localhost", server.port)

      proxy.closeNow()
      assert(proxy.awaitShutdown(1.second), "Join on proxy timed out")
      assert(reader.read() == -1, "Client connection did not get closed")
    }
  }

  test("Invalid hostname returns 502 Bad Gateway") {
    for (proxy <- managed(new TlsProxy(proxyPort))) {
      executor.execute(proxy)
      Thread.sleep(100)

      for (client <- managed(new Socket("localhost", proxyPort))) {
        val writer = new OutputStreamWriter(client.getOutputStream, StandardCharsets.US_ASCII)
        val reader = new BufferedReader(new InputStreamReader(client.getInputStream))

        writer.write(s"CONNECT surely-this-doesnt-resolve:443 HTTP/1.1\r\n\r\n")
        writer.flush()

        val line = reader.readLine()
        assert("^HTTP/1.1 502 .+".r.findFirstMatchIn(line).isDefined, s"Unexpected connect response: $line")

        while (reader.readLine() != null) {}  // consume remaining headers and body
      }
    }
  }

  private def proxyConnect(host: String, port: Int): (Socket, Writer, BufferedReader) = {
    val client = new Socket("localhost", proxyPort)
    val writer = new OutputStreamWriter(client.getOutputStream, StandardCharsets.US_ASCII)
    val reader = new BufferedReader(new InputStreamReader(client.getInputStream))

    writer.write(s"CONNECT $host:$port HTTP/1.1\r\n\r\n")
    writer.flush()

    val line = reader.readLine()
    assert(connectPattern.findFirstMatchIn(line).isDefined, s"Unexpected connect response: $line")
    while (reader.readLine() != "") {}  // consume remaining headers and body

    (client, writer, reader)
  }

  after {
    executor.shutdown()
    if (!executor.awaitTermination(1, TimeUnit.SECONDS))
      logger.error("Failed to shut down executor")
  }

  private def getAvailablePort: Int = {
    val socket = new ServerSocket(0)
    val port = socket.getLocalPort
    socket.close()
    port
  }
}
