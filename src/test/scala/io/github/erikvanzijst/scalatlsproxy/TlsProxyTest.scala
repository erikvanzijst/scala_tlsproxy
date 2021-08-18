package io.github.erikvanzijst.scalatlsproxy

import org.scalatest.funsuite.AnyFunSuite

class TlsProxyTest extends AnyFunSuite {

  test("Run standalone proxy") {
    new TlsProxy(3128).run()
  }
}
