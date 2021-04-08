package org.blackbox

import cats.effect.{ContextShift, IO, Timer}
import log.effect.LogWriter

import log.effect.fs2.SyncLogWriter._

import scala.concurrent.ExecutionContext.global

trait Env {
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO]                 = IO.timer(global)
  implicit val log: LogWriter[IO]               = consoleLog[IO]
}
