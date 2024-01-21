package com.cqrs.demo.stream

import play.api.libs.json._

case class Person(id: Int, name: String)

object Person {
  implicit val personReads: Reads[Person] = Json.reads[Person]

  implicit val personWrites: Writes[Person] = OWrites(person =>
    Json.obj(
      "schema" -> schema,
      "payload" -> Json.toJson(person)(Json.writes[Person])
    )
  )

  private val schema: JsValue = Json.obj(
    "type" -> "struct",
    "name" -> "User",
    "fields" -> Json.arr(
      Json.obj("field" -> "id", "type" -> "int32", "optional" -> false),
      Json.obj("field" -> "name", "type" -> "string", "optional" -> false)
    )
  )


  def forwards(event: Event): Seq[(String, Person)] = {
    Seq(
      (event.followerID, Person(id = event.followerID.toInt, name = event.followerName)),
      (event.followeeID, Person(id = event.followeeID.toInt, name = event.followeeName))
    )
  }
}

