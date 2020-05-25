name := "hw-scala"

version := "1.0"

scalaVersion := "2.13.2"

lazy val akkaVersion = "2.6.5"
lazy val akkaGrpcVersion = "0.8.4"

enablePlugins(AkkaGrpcPlugin)

// ALPN agent
//enablePlugins(JavaAgent)
//javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.10" % "runtime;test"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "com.github.tomakehurst" % "wiremock" % "2.26.3" % "test"
)