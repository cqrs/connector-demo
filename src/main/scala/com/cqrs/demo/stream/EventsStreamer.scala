package com.cqrs.demo.stream

import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.Serdes._

import java.util.Properties

object EventsStreamer {
  def main(args: Array[String]): Unit = {
    val parsedArgs = parseArgs(args)
    val builder = new StreamsBuilder()
    val props = new Properties()

    // Load properties from the file
    props.load(getClass.getClassLoader.getResourceAsStream("kafka-streams.properties"))

    // Set up additional properties if needed
    // props.put(StreamsConfig.OTHER_CONFIG, "value")

    // Use the loaded properties to configure Kafka Streams
    val config = new StreamsConfig(props)

    // Parse arguments
    parsedArgs.get("--help") match {
      case Some(_) => printHelp()
      case None    => {}
    }
    var topic: String = ""
    parsedArgs.get("--topic") match {
      case Some(Some(topicType)) =>
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, s"events-streamer-$topicType")
        topic = topicType

      case _ =>
        println("Error: Invalid or missing --topic flag")
        System.exit(1)
    }

    // Forward stream to appropriate topic per the flag
    val sourceStream: KStream[String, String] = builder.stream[String, String]("events")
    val stream: KStream[String, String] = sourceStream.flatMap { case (_, json) =>
      Event.parseJson(json).toSeq.flatMap { event =>
        topic match {
          case "jdbc"  => JDBC.forwards(event)
          case "graph" => Graph.forwards(event)
        }
      }
    }
    stream.to(topic)

    // Build and start the Kafka Streams application
    val streams = new KafkaStreams(builder.build(), props)
    streams.start()

    // Add shutdown hook for graceful closure
    sys.addShutdownHook {
      streams.close()
    }
  }

  private def printHelp(): Unit = {
    println("Usage: MyApp [options]")
    println("Options:")
    println("  --help          Display this help message")
    println("  --topic <value> Set the topic to forward to")
  }

  private def parseArgs(args: Array[String]): Map[String, Option[String]] = {
    val argsList = args.toList
    val argPairs = argsList.zipWithIndex.flatMap {
      case (arg, idx) if arg.startsWith("--") =>
        val value = argsList.drop(idx + 1).headOption.filterNot(_.startsWith("--"))
        Some(arg -> value)
      case _ => None
    }
    argPairs.toMap
  }
}
