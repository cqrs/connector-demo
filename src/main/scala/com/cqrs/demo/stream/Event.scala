package com.cqrs.demo.stream

import play.api.libs.json._

case class Event(
    followerID: String,
    followerName: String,
    followeeID: String,
    followeeName: String
)

object Event {
  implicit val eventReads: Reads[Event] = Json.reads[Event]
  implicit val eventWrites: Writes[Event] = Json.writes[Event]

}
