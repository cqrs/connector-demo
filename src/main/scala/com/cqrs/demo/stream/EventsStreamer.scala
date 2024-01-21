package com.cqrs.demo.stream

import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.common.serialization.Serdes

import java.util.Properties

object EventsStreamer {
  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.load(getClass.getClassLoader.getResourceAsStream("kafka-streams.properties"))

    val builder = new StreamsBuilder()

    val sourceStream: KStream[String, Option[Event]] =
      builder
        .stream[String, Event]("events")(Consumed.`with`(Serdes.String, Event.jsonSerde))
        .mapValues(event => Option(event))

    // Process and send to 'jdbc' topic
    sourceStream
      .flatMap { (_, maybeEvent) => maybeEvent.map(event => Person.forwards(event)).getOrElse(Seq.empty) }
      .to("people")(Produced.`with`(Serdes.String, Person.jsonSerde))

    // Process and send to 'graph' topic
    sourceStream
      .flatMap { (_, maybeEvent) => maybeEvent.map(event => Node.forwards(event)).getOrElse(Seq.empty) }
      .to("graph")(Produced.`with`(Serdes.String, Node.jsonSerde))
    sourceStream
      .flatMap { (_, maybeEvent) => maybeEvent.map(event => Relationship.forwards(event)).getOrElse(Seq.empty) }
      .to("graph")(Produced.`with`(Serdes.String, Relationship.jsonSerde))

    // Build and start the Kafka Streams application
    val streams = new KafkaStreams(builder.build(), new StreamsConfig(props))
    streams.start()

    // Add shutdown hook for graceful closure
    sys.addShutdownHook {
      streams.close()
    }
  }
}
