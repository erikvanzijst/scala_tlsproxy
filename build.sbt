name := "scala-tlsproxy"
organization := "io.github.erikvanzijst"

version := "0.2.0-SNAPSHOT"

scalaVersion := "2.12.14"

libraryDependencies += "org.scalatest" %% "scalatest-funsuite" % "3.2.9" % "test"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "[3.0,)"

credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials")

ThisBuild / organization := "io.github.erikvanzijst"
ThisBuild / organizationName := "erikvanzijst"
ThisBuild / organizationHomepage := Some(url("https://github.com/erikvanzijst"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/erikvanzijst/scala_tlsproxy"),
    "scm:git@github.com:erikvanzijst/scala_tlsproxy.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "erikvanzijst",
    name  = "Erik van Zijst",
    email = "erik.van.zijst@gmail.com",
    url   = url("https://github.com/erikvanzijst")
  )
)

ThisBuild / description := "Very simple HTTPS proxy server lib written in Scala 2.12 with no external dependencies."
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/erikvanzijst/scala_tlsproxy"))

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true