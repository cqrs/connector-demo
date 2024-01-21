package com.cqrs.demo.stream

import play.api.libs.json._
import org.apache.kafka.common.serialization._

object SerDes {
  def jsonSerde[T](implicit reads: Reads[T], writes: Writes[T]): Serde[T] =
    Serdes.serdeFrom(new JsonSerializer[T], new JsonDeserializer[T])

  class JsonSerializer[T](implicit writes: Writes[T]) extends Serializer[T] {
    override def serialize(topic: String, data: T): Array[Byte] =
      Json.toJson(data).toString().getBytes
    override def close(): Unit = {}
  }

  class JsonDeserializer[T](implicit reads: Reads[T]) extends Deserializer[T] {
    override def deserialize(topic: String, data: Array[Byte]): T =
      Json.parse(new String(data)).validate[T].getOrElse(null.asInstanceOf[T])
    override def close(): Unit = {}
  }
}
