package org.blackbox

import cats.data.Kleisli
import cats.effect.concurrent.Ref
import cats.syntax.functor._
import cats.{Defer, Monad, Monoid}
import io.circe.generic.auto._
import io.circe.{Encoder, Printer}
import org.blackbox.Routes.EventMetric
import org.http4s.circe.jsonEncoderWithPrinterOf
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{EntityEncoder, HttpRoutes, Request, Response}

import scala.concurrent.duration.FiniteDuration

sealed abstract class Routes[F[_]](state: Ref[F, State], windowSize: FiniteDuration) {

  implicit def circeEntityEncoder[A: Encoder]: EntityEncoder[F, A] =
    jsonEncoderWithPrinterOf(Printer.spaces2)

  def routes(implicit defer: Defer[F], F: Monad[F]): Kleisli[F, Request[F], Response[F]] =
    HttpRoutes
      .of[F] {
        case GET -> Root / "metrics" =>
          state.get.map { state =>
            val metrics = state.map {
              case (eventType, wordsFrequency) =>
                EventMetric(
                  eventType = eventType,
                  validFrom = wordsFrequency.firstKey,
                  validTo = wordsFrequency.firstKey + windowSize.toSeconds,
                  wordFrequency =
                    wordsFrequency.values.foldLeft(Map.empty[String, Int])(Monoid[Map[String, Int]].combine)
                )
            }.toSeq
            Response(
              status = Ok,
              body = EntityEncoder[F, Seq[EventMetric]].toEntity(metrics).body
            )
          }
      }
      .orNotFound
}

object Routes {
  def apply[F[_]](state: Ref[F, State], windowSize: FiniteDuration): Routes[F] =
    new Routes[F](state, windowSize) {}

  case class EventMetric(eventType: String, validFrom: Long, validTo: Long, wordFrequency: Map[String, Int])
}
