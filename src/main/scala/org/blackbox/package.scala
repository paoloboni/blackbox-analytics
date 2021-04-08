package org

import io.circe.Decoder

import scala.collection.immutable.SortedMap

package object blackbox {

  type State = Map[String, SortedMap[Long, Map[String, Int]]]

  object State {
    val Empty: State = Map.empty
  }

  case class Event(event_type: String, data: String, timestamp: Long)

  object Event {
    import io.circe.generic.semiauto._
    implicit val decoder: Decoder[Event] = deriveDecoder
  }
}
