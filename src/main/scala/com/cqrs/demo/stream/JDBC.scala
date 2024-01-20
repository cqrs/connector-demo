package com.cqrs.demo.stream

import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.Serdes._
import play.api.libs.json.{Json, Writes}

case class Person(id: String, name: String)

object JDBC {
  implicit val personWrites: Writes[Person] = Json.writes[Person]

  def forwards(event: Event): Seq[(String, String)] = {
    val person1 = Person(id = event.followerID, name = event.followerName)
    val person2 = Person(id = event.followeeID, name = event.followeeName)

    Seq(
      (person1.id, Json.toJson(person1).toString),
      (person2.id, Json.toJson(person2).toString)
    )
  }
}
