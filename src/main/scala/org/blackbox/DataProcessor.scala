package org.blackbox

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Sync}
import cats.syntax.all._
import fs2.{Pipe, Stream, text}
import io.circe.parser

import scala.collection.immutable.SortedMap
import scala.concurrent.duration.FiniteDuration

sealed abstract class DataProcessor[F[_]](state: Ref[F, State], windowSize: FiniteDuration) {

  def run(bytes: Stream[F, Byte])(implicit F: Concurrent[F]): F[State] = {

    def decodeEvent: Pipe[F, String, Event] = _.map(parser.decode[Event]).map(_.toSeq).flatMap(Stream.iterable)

    val groupLimit    = 1 // increase this to process higher throughput
    val maxConcurrent = 5

    bytes
      .through(text.utf8Decode)
      .through(text.lines)
      .through(decodeEvent)
      .groupAdjacentByLimit(groupLimit)(_.event_type)
      .mapAsyncUnordered(maxConcurrent) {
        case (eventType, events) =>
          Stream
            .chunk(events)
            .map(e => SortedMap(e.timestamp -> Map(e.data -> 1)))
            .foldMonoid
            .evalMap { increment: SortedMap[Long, Map[String, Int]] =>
              state.getAndUpdate { st =>
                st.updatedWith(eventType) { eventTypeState =>
                  val updated = eventTypeState.fold(increment)(_ |+| increment)
                  Some(updated.rangeFrom(updated.lastKey - windowSize.toSeconds + 1))
                }
              }
            }
            .compile
            .drain
      }
      .compile
      .drain *> state.get
  }
}

object DataProcessor {
  def apply[F[_]: Sync](state: Ref[F, State], windowSize: FiniteDuration): DataProcessor[F] =
    new DataProcessor[F](state, windowSize) {}
}
