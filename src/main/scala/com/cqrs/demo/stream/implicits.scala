package com.cqrs.demo.stream

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.serialization.Serdes
import play.api.libs.json.{Json, Reads, Writes}

object implicits {
  implicit def jsonSerde[T >: Null](
    implicit reads: Reads[T],
    writes: Writes[T]
  ): Serde[T] =
    Serdes.fromFn[T](
      (data: T) => Json.toJson(data).toString().getBytes,
      (data: Array[Byte]) => Json.parse(new String(data)).validate[T].asOpt
    )
}
