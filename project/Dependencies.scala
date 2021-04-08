import sbt._

object Dependencies {
  lazy val http4sVersion    = "0.21.20"
  lazy val logEffectVersion = "0.14.1"
  lazy val circeVersion     = "0.13.0"
  lazy val declineVersion   = "1.3.0"

  lazy val http4sDsl         = "org.http4s"              %% "http4s-dsl"          % http4sVersion
  lazy val http4sBlazeServer = "org.http4s"              %% "http4s-blaze-server" % http4sVersion
  lazy val circe             = "io.circe"                %% "circe-generic"       % circeVersion
  lazy val circeParser       = "io.circe"                %% "circe-parser"        % circeVersion
  lazy val http4sCirce       = "org.http4s"              %% "http4s-circe"        % http4sVersion
  lazy val fs2               = "co.fs2"                  %% "fs2-core"            % "2.5.3"
  lazy val logEffectCore     = "io.laserdisc"            %% "log-effect-core"     % logEffectVersion
  lazy val logEffectFs2      = "io.laserdisc"            %% "log-effect-fs2"      % logEffectVersion
  lazy val decline           = "com.monovore"            %% "decline"             % declineVersion
  lazy val declineEffect     = "com.monovore"            %% "decline-effect"      % declineVersion
  lazy val declineRefined    = "com.monovore"            %% "decline-refined"     % declineVersion
  lazy val log4jSlf4j        = "org.apache.logging.log4j" % "log4j-slf4j-impl"    % "2.14.1"

  lazy val scalaTest           = "org.scalatest"              %% "scalatest"                 % "3.2.5"
  lazy val scalacheck          = "org.scalacheck"             %% "scalacheck"                % "1.15.3"
  lazy val scalacheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.5"
  lazy val scalatestPlus       = "org.scalatestplus"          %% "scalacheck-1-15"           % "3.2.5.0"
}
