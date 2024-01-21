package com.cqrs.demo.stream

import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.Serdes._


import java.util.Properties

object EventsStreamer {
  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.load(getClass.getClassLoader.getResourceAsStream("kafka-streams.properties"))

    val builder = new StreamsBuilder()
    val sourceStream: KStream[String, String] = builder.stream[String, String]("events")

    // Parse the JSON once
    val parsedStream: KStream[String, Option[Event]] = sourceStream.mapValues(Event.parseJson _)

    // Process and send to 'jdbc' topic
    parsedStream
      .flatMap { (_, maybeEvent) =>
        maybeEvent.map { event => JDBC.forwards(event) }.getOrElse(Seq.empty)
      }
      .to("jdbc")

    // Process and send to 'graph' topic
    parsedStream
      .flatMap { (_, maybeEvent) =>
        maybeEvent.map { event => Graph.forwards(event) }.getOrElse(Seq.empty)
      }
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
