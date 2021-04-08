package org.blackbox

import cats.effect.IO
import cats.effect.concurrent.Ref
import io.circe.generic.auto._
import org.blackbox.Routes.EventMetric
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.implicits._
import org.scalatest.Assertion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.immutable.SortedMap
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class RoutesSpec extends AnyFreeSpec with Matchers with Env {
  "empty state should render empty array" in {
    val response = runRequest(State.Empty, 1.second)
    check(response, Status.Ok, Seq.empty[EventMetric])
  }
  "non-empty state should render compacted state" in {
    val state = Map(
      "foo" -> SortedMap(1L -> Map("dolor" -> 1), 2L -> Map("dolor" -> 1, "lorem" -> 2)),
      "baz" -> SortedMap(2L -> Map("ipsum" -> 1))
    )
    val windowSize = 10.second
    val response   = runRequest(state, windowSize)
    check(
      response,
      Status.Ok,
      Seq(
        EventMetric(eventType = "foo", validFrom = 1, validTo = 11, wordFrequency = Map("dolor" -> 2, "lorem" -> 2)),
        EventMetric(eventType = "baz", validFrom = 2, validTo = 12, wordFrequency = Map("ipsum" -> 1))
      )
    )
  }

  private def runRequest(state: State, windowSize: FiniteDuration): IO[Response[IO]] = {
    for {
      state <- Ref[IO].of(state)
      routes = Routes[IO](state, windowSize)
      response <- routes.routes.run(Request(method = Method.GET, uri = uri"/metrics"))
    } yield response
  }

  private def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: A)(implicit
      ev: EntityDecoder[IO, A]
  ): Assertion = {
    val actualResp = actual.unsafeRunSync()
    actualResp.status should ===(expectedStatus)
    expectedBody should ===(actualResp.as[A].unsafeRunSync())
  }
}
