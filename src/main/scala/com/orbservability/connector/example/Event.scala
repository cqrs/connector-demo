package com.orbservability.connector.demo

case class Event(
  followerID: String,
  followerName: String,
  followeeID: String,
  followeeName: String,

)

object Event {
  def fromMap(valueMap: scala.collection.mutable.Map[String,Any]): Event = {
    Event(
      followerID = valueMap("followerID").toString,
      followerName = valueMap("followerName").toString,
      followeeID = valueMap("followeeID").toString,
      followeeName = valueMap("followeeName").toString,
    )
  }
}
