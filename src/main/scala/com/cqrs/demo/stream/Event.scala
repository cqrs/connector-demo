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

  def parseJson(jsonString: String): Option[Event] = {
    val json: JsValue = Json.parse(jsonString)

    json.validate[Event] match {
      case JsSuccess(event, _) => Some(event)
      case JsError(_)          => None
    }
  }
}
