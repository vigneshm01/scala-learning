import Dependencies._
import MyUtil._

ThisBuild / organization := "dev.insideyou"
ThisBuild / scalaVersion := "2.13.13"

lazy val `json-playground` =
  project
    .in(file("."))
    .dependsOn(macros % Cctt)
    .settings(name := "JSON PlayGround")
    .settings(commonSettings)
    .settings(autoImportSettings)
    .settings(dependencies)
    .aggregate(macros)

lazy val macros =
  project
    .in(file("macros"))
    .settings(commonSettings)
    .settings(dependencies)

lazy val commonSettings = {
  lazy val commonCompilerPlugins = Seq(
    addCompilerPlugin(com.olegpy.`better-monadic-for`),
    addCompilerPlugin(org.augustjune.`context-applied`),
    addCompilerPlugin(org.typelevel.`kind-projector`),
  )

  lazy val commonScalacOptions = Seq(
    Compile / console / scalacOptions := {
      (Compile / console / scalacOptions)
        .value
        .filterNot(_.contains("wartremover"))
        .filterNot(Scalac.Lint.toSet)
        .filterNot(Scalac.FatalWarnings.toSet) :+ "-Wconf:any:silent"
    },
    Test / console / scalacOptions :=
      (Compile / console / scalacOptions).value,
  )

  lazy val otherCommonSettings = Seq(
    update / evictionWarningOptions := EvictionWarningOptions.empty,
//    scalacOptions += s"-Wconf:src=${target.value}/.*:s",
  )

  Seq(
    commonCompilerPlugins,
    commonScalacOptions,
    otherCommonSettings,
  ).reduceLeft(_ ++ _)
}

lazy val autoImportSettings = Seq(
  scalacOptions +=
    Seq(
      "java.lang",
      "scala",
      "scala.Predef",
      "scala.annotation",
      "scala.util.chaining",
    ).mkString(start = "-Yimports:", sep = ",", end = ""),
  Test / scalacOptions +=
    Seq(
      "derevo",
      "derevo.scalacheck",
      "org.scalacheck",
      "org.scalacheck.Prop",
    ).mkString(start = "-Yimports:", sep = ",", end = ""),
)

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    // main dependencies
    "io.circe" %% "circe-config" % "0.8.0",
    "org.typelevel" %% "cats-effect" % "2.1.2"
  ),
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-fs2",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-refined"
  ).map(_ % "0.13.0"),
  libraryDependencies ++= Seq(
    com.eed3si9n.expecty.expecty,
    org.scalacheck.scalacheck,
    org.scalameta.`munit-scalacheck`,
    org.scalameta.munit,
    org.typelevel.`discipline-munit`,
    tf.tofu.`derevo-scalacheck`,
  ).map(_ % Test),
)
