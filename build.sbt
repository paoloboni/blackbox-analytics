import Dependencies._

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "org.blackbox"
ThisBuild / organizationName := "blackbox"

lazy val root = (project in file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "blackbox-analytics",
    libraryDependencies ++= Seq(
      http4sDsl,
      http4sBlazeServer,
      http4sCirce,
      circe,
      circeParser,
      fs2,
      logEffectCore,
      logEffectFs2,
      decline,
      declineEffect,
      declineRefined,
      log4jSlf4j,
      scalaTest           % Test,
      scalacheck          % Test,
      scalacheckShapeless % Test,
      scalatestPlus       % Test
    ),
    Compile / mainClass := Some("org.blackbox.Main")
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
