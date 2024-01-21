package com.cqrs.demo.stream

import play.api.libs.json._
import org.apache.kafka.common.serialization.Serde

case class Event(
    followerID: String,
    followerName: String,
    followeeID: String,
    followeeName: String
)

object Event {
  implicit val eventReads: Reads[Event] = Json.reads[Event]
  implicit val eventWrites: Writes[Event] = Json.writes[Event]

  val jsonSerde: Serde[Event] = SerDes.jsonSerde[Event]
}
