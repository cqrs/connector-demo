package com.cqrs.demo.stream

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ObjectMapper
import play.api.libs.json._
import org.apache.kafka.common.serialization._

case class Person(id: Int, name: String) {
  val schema: JsValue = Json.obj(
    "type" -> "struct",
    "name" -> "User",
    "fields" -> Json.arr(
      Json.obj("field" -> "id", "type" -> "int32", "optional" -> false),
      Json.obj("field" -> "name", "type" -> "string", "optional" -> false)
    )
  )

  def withSchema: JsValue = {
    Json.obj(
      "schema" -> schema,
      "payload" -> Json.toJson(this)
    )
  }
}

object Person {
  implicit val personReads: Reads[Person] = Json.reads[Person]
  implicit val personWrites: Writes[Person] = Json.writes[Person]

  val jsonSerde: Serde[Person] = Serdes.serdeFrom(new PersonJsonSerializer(), new SerDes.JsonDeserializer())

  def forwards(event: Event): Seq[(String, Person)] = {
    Seq(
      (event.followerID, Person(id = event.followerID.toInt, name = event.followerName)),
      (event.followeeID, Person(id = event.followeeID.toInt, name = event.followeeName))
    )
  }
}

class PersonJsonSerializer extends Serializer[Person] {
  private val objectMapper = new ObjectMapper()
  override def serialize(topic: String, data: Person): Array[Byte] = {
    data.withSchema.toString().getBytes
  }
  override def close(): Unit = {}
}
