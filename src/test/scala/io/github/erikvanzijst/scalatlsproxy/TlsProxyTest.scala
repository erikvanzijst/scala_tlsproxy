package io.github.erikvanzijst.scalatlsproxy

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import java.net.{ServerSocket, Socket}
import java.nio.charset.StandardCharsets
import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

class TlsProxyTest extends AnyFunSuite with BeforeAndAfter with StrictLogging {
  private implicit var executor: ExecutorService = _
  private val connectPattern = "^HTTP/1.1 200 .+".r

  before {
    executor = Executors.newFixedThreadPool(10)
  }
//
//  test("Run standalone proxy") {
//    new TlsProxy(3128).run()
//  }

  test("Happy path") {
    val server = new EchoServer()
    val proxyPort = getAvailablePort

    val proxy = new TlsProxy(proxyPort)
    executor.execute(proxy)
    Thread.sleep(100)

    val conn = new Socket("localhost", proxyPort)
    val writer = new OutputStreamWriter(conn.getOutputStream, StandardCharsets.US_ASCII)
    val reader = new BufferedReader(new InputStreamReader(conn.getInputStream))

    writer.write(
      s"CONNECT localhost:${server.port} HTTP/1.1\r\n" +
        "\r\n")
    writer.flush()

    readConnectResponse(reader)

    writer.write("Hello\n")
    writer.flush()
    assert(reader.readLine() == "Hello")
  }

  after {
    executor.shutdown()
    if (!executor.awaitTermination(1, TimeUnit.SECONDS))
      logger.error("Failed to shut down executor")
  }

  private def readConnectResponse(reader: BufferedReader): Unit = {
    val line = reader.readLine()
    assert(connectPattern.findFirstMatchIn(line).isDefined, s"Unexpected connect response: $line")
    while (reader.readLine() != "") {}
  }

  private def getAvailablePort: Int = {
    val socket = new ServerSocket(0)
    val port = socket.getLocalPort
    socket.close()
    port
  }
}
