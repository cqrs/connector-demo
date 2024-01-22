package com.cqrs.demo.stream

import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import java.util.Properties
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.serialization.Serdes._
import com.cqrs.demo.stream.implicits._

object EventsStreamer {

  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.load(getClass.getClassLoader.getResourceAsStream("kafka-streams.properties"))

    val builder = new StreamsBuilder()

    val sourceStream: KStream[String, Event] =
      builder
        .stream[String, Event]("events")

    // Process and send to 'jdbc' topic
    sourceStream
      .flatMap((_, event) => Person.forwards(event))
      .to("people")

    // Process and send to 'graph' topic
    sourceStream
      .flatMap((_, event) => Node.forwards(event))
      .to("graph")
    sourceStream
      .flatMap((_, event) => Relationship.forwards(event))
      .to("graph")

    // Build and start the Kafka Streams application
    val streams = new KafkaStreams(builder.build(), new StreamsConfig(props))
    streams.start()

    // Add shutdown hook for graceful closure
    sys.addShutdownHook {
      streams.close()
    }
  }
}
