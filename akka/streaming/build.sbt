course := "reactive"
assignment := "streaming"

testFrameworks += new TestFramework("munit.Framework")
Test / parallelExecution := false

val akkaVersion = "2.6.9"

scalaVersion := "2.13.5"
scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-Xlint",
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
  "org.scalameta" %% "munit" % "0.7.22" % Test,
  "org.slf4j" % "slf4j-simple" % "1.7.28"
)
