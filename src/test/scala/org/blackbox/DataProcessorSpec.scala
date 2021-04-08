package org.blackbox

import cats.effect.IO
import cats.effect.concurrent.Ref
import fs2.Stream
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.immutable.SortedMap
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class DataProcessorSpec extends AnyFreeSpec with Matchers with Env {

  "should process events included in the same time window" in {

    val windowSize = 2.second

    val e1 = Seq(
      """{ "event_type": "baz", "data": "amet", "timestamp": 1 }""",
      """{ "7�N�+����""",
      """{ "cw~�<��>�0""",
      """{ "��=����"""
    )

    val st1 = runProcess(initialState = State.Empty, rawEvents = e1, windowSize = windowSize)

    st1 should ===(Map("baz" -> SortedMap(1L -> Map("amet" -> 1))))

    val e2 = Seq(
      """{ "event_type": "foo", "data": "lorem", "timestamp": 2 }""",
      """{ "event_type": "baz", "data": "ipsum", "timestamp": 2 }""",
      """{ "event_type": "foo", "data": "dolor", "timestamp": 2 }""",
      """{ "event_type": "foo", "data": "lorem", "timestamp": 2 }"""
    )

    val st2 = runProcess(initialState = st1, rawEvents = e2, windowSize = windowSize)
    st2 should ===(
      Map(
        "foo" -> SortedMap(2L -> Map("dolor" -> 1, "lorem" -> 2)),
        "baz" -> SortedMap(1L -> Map("amet" -> 1), 2L -> Map("ipsum" -> 1))
      )
    )
  }

  "should process events spanning different time windows" in {

    val windowSize = 1.second

    val e1 = Seq(
      """{ "event_type": "baz", "data": "amet", "timestamp": 1 }""",
      """{ "7�N�+����""",
      """{ "cw~�<��>�0""",
      """{ "��=����""",
      """{ "event_type": "foo", "data": "lorem", "timestamp": 2 }"""
    )

    val st1 = runProcess(initialState = State.Empty, rawEvents = e1, windowSize = windowSize)

    st1 should ===(
      Map(
        "foo" -> SortedMap(2L -> Map("lorem" -> 1)),
        "baz" -> SortedMap(1L -> Map("amet" -> 1))
      )
    )

    val e2 = Seq(
      """{ "event_type": "baz", "data": "ipsum", "timestamp": 2 }""",
      """{ "event_type": "foo", "data": "dolor", "timestamp": 2 }""",
      """{ "event_type": "foo", "data": "lorem", "timestamp": 2 }"""
    )

    val st2 = runProcess(initialState = st1, rawEvents = e2, windowSize = windowSize)
    st2 should ===(
      Map(
        "foo" -> SortedMap(2L -> Map("dolor" -> 1, "lorem" -> 2)),
        "baz" -> SortedMap(2L -> Map("ipsum" -> 1))
      )
    )
  }

  "time windows should be sliding" in {

    val windowSize = 2.second

    val e1 = Seq(
      """{ "event_type": "baz", "data": "amet", "timestamp": 1 }""",
      """{ "7�N�+����""",
      """{ "cw~�<��>�0""",
      """{ "��=����""",
      """{ "event_type": "baz", "data": "lorem", "timestamp": 2 }"""
    )

    val st1 = runProcess(initialState = State.Empty, rawEvents = e1, windowSize = windowSize)

    st1 should ===(
      Map(
        "baz" -> SortedMap(1L -> Map("amet" -> 1), 2L -> Map("lorem" -> 1))
      )
    )

    val e2 = Seq(
      """{ "event_type": "baz", "data": "ipsum", "timestamp": 3 }"""
    )

    val st2 = runProcess(initialState = st1, rawEvents = e2, windowSize = windowSize)
    st2 should ===(
      Map(
        "baz" -> SortedMap(2L -> Map("lorem" -> 1), 3L -> Map("ipsum" -> 1))
      )
    )
  }

  private def runProcess(windowSize: FiniteDuration, initialState: State, rawEvents: Seq[String]): State =
    (for {
      state <- Ref[IO].of(initialState)
      processor = DataProcessor[IO](state, windowSize)
      bytes     = Stream(rawEvents: _*).intersperse("\n").flatMap(s => Stream(s.getBytes("UTF-8"): _*))
      finalState <- processor.run(bytes)
    } yield finalState).unsafeRunSync()

}
