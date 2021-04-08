package org.blackbox

import cats.effect._
import cats.effect.concurrent.Ref
import com.monovore.decline._
import com.monovore.decline.effect._
import com.monovore.decline.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import fs2.io.stdin
import log.effect.LogWriter
import log.effect.fs2.SyncLogWriter.consoleLog
import org.http4s.server.blaze._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt

object Main extends CommandIOApp(name = "blackbox-analytics", header = "Analytics for blackbox processes") {
  private implicit val log: LogWriter[IO] = consoleLog[IO]

  private val windowOpt =
    Opts.option[Int Refined Positive](
      "window",
      short = "w",
      metavar = "seconds",
      help = "Set the time-window size in seconds for grouping the event data"
    )

  override def main: Opts[IO[ExitCode]] =
    windowOpt.map { windowSize =>
      val windowSizeDuration = windowSize.value.seconds
      Blocker[IO].use(blocker =>
        for {
          state <- Ref[IO].of(State.Empty)
          bytes = stdin[IO](4096, blocker)
          _ <- DataProcessor[IO](state, windowSizeDuration).run(bytes).start
          server <-
            BlazeServerBuilder[IO](global)
              .bindHttp(8080, "localhost")
              .withHttpApp(Routes[IO](state, windowSizeDuration).routes)
              .resource
              .use(_ => IO.never)
              .as(ExitCode.Success)
        } yield server
      )
    }
}
