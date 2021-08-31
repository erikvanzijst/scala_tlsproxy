package io.github.erikvanzijst

import java.io.Reader
import java.util.Properties
import scala.io.Source.fromURL

package object scalatlsproxy {

  private val reader: Reader = fromURL(getClass.getResource("/proxy.properties")).reader()
  val properties = new Properties()
  properties.load(reader)

  val NAME: String = properties.getProperty("name")
  val VERSION: String = properties.getProperty("version")
  val BUILD_DATE: String = properties.getProperty("buildDate")

  reader.close()
}
