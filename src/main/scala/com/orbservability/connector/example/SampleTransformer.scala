package com.orbservability.connector.example

import java.util
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.data.Struct
import scala.collection.JavaConverters._
import org.apache.kafka.connect.transforms.Transformation
import play.api.libs.json.{JsValue, Json}
import org.apache.kafka.common.config.ConfigDef

class SampleTransformer[R <: ConnectRecord[R]] extends Transformation[R] {

  override def apply(record: R): R = {
    val mapValue = Event.fromMap(record.value().asInstanceOf[java.util.Map[String, Any]].asScala)
    val events = handleRecord(mapValue)

    record.newRecord(
      record.topic,
      record.kafkaPartition,
      record.keySchema,
      record.key,
      record.valueSchema,
      Json.obj("events" -> events).toString,
      record.timestamp
    )
  }

  override def close: Unit = {}

  override def configure(configs: util.Map[String, _]): Unit = {}

  override def config: ConfigDef = {
    new ConfigDef()
  }

  private def handleRecord(record: Event): JsValue = {
    Json.arr(
      Json.obj(
        "op" -> "merge",
        "type" -> "node",
        "labels" -> Json.arr("Person"),
        "ids" -> Json.obj(
          "uuid" -> record.followerID,
        ),
        "properties" -> Json.obj(
          "name" -> record.followerName,
        ),
      ),
      Json.obj(
        "op" -> "merge",
        "type" -> "node",
        "labels" -> Json.arr("Person"),
        "ids" -> Json.obj(
          "uuid" -> record.followeeID,
        ),
        "properties" -> Json.obj(
          "name" -> record.followeeName,
        ),
      ),
      Json.obj(
        "op" -> "merge",
        "type" -> "relationship",
        "rel_type" -> "FOLLOWS",
        "from" -> Json.obj(
          "labels" -> Json.arr("Person"),
          "ids" -> Json.obj(
            "uuid" -> record.followerID
          )
        ),
        "to" -> Json.obj(
          "labels" -> Json.arr("Person"),
          "ids" -> Json.obj(
            "uuid" -> record.followeeID
          )
        )
      )
    )
  }
}
