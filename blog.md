# Kafka Connect / Streams: A Beginner's Guide

## Introduction

In the ever-evolving world of data, [Apache Kafka API](https://kafka.apache.org/) has emerged as a leading platform for handling real-time data streams. It is powerful yet can seem daunting to newcomers. 

Kafka has fostered an extensive ecosystem around its APIs, with numerous tools, extensions, and third-party integrations. Its API has become a standard for other systems; many technologies now offer Kafka-compatible APIs to integrate seamlessly with the Kafka ecosystem. Several implementations exist with both self-hosting and SaaS (Sofware as a Service) options:

- [Confluent](https://www.confluent.io/)
- [Redpanda](https://redpanda.com/)
- [Azure Event Hubs](https://azure.microsoft.com/en-us/products/event-hubs)
- [Amazon Managed Streaming for Apache Kafka (MSK)](https://aws.amazon.com/msk)
- [IBM Event Streams](https://www.ibm.com/products/event-automation)
- [Aiven](https://aiven.io/kafka)

One key feature of Kafka is its ability to connect with various data **sources** and **sinks** via Kafka Connect. While Kafka Connect is great for moving data between Kafka and external systems, it's often necessary to include Kafka Streams - which plays a crucial role in processing and manipulating data _within_ Kafka. This guide will walk you through just what Kafka Connect and Streams are all about, and how you can set them up, even if you're starting from scratch.

## What is Kafka Connect?

Kafka Connect is a set of ready-to-use components that link Kafka with external systems like databases, key-value stores, search indexes, and file systems. It simplifies the process of importing data into Kafka (source connectors) and exporting data from Kafka to external systems (sink connectors).

When working with Kafka Connect, it's important to understand the plugins that make up the connector pipeline. These plugins include:

### Converters

Converters are responsible for serializing and deserializing data. They convert data from the format used by Kafka to the format required by the external system, and vice versa. Common converters include [String, JSON, Avro, and Protobuf](https://docs.redpanda.com/current/deploy/deployment-option/cloud/managed-connectors/converters-and-serialization/). Choosing the right converter is essential for ensuring that data is correctly interpreted and formatted when moving between Kafka and external systems. Here's a [deep dive into converters and serialization](https://www.confluent.io/blog/kafka-connect-deep-dive-converters-serialization-explained/).

### Transforms

Transforms, also known as Single Message Transforms (SMTs), allow you to modify the data as it passes through the connector. This can include simple changes like renaming fields or more complex alterations like filtering or aggregating data. Transforms are applied at the record level and can be used in both source and sink connectors. They provide a powerful way to preprocess data before it is consumed by external systems or by Kafka. [Learn how to use SMTs](https://www.confluent.io/blog/kafka-connect-single-message-transformation-tutorial-with-examples).

### Connectors

Connectors serve as the key components that manage the interaction with external data sources and sinks. They abstract away the complexities of interfacing with different systems and define the Domain-Specific Language (DSL) for extracting or ingesting data. Each connector is tailored to work with specific external systems, such as databases, file systems, or cloud services.

- **Source Connectors**

  Responsible for importing data from external systems into Kafka topics. They monitor the source system for new data and automatically fetch it into Kafka, ensuring a continuous flow of data into the Kafka ecosystem.

- **Sink Connectors**

  Used to export data from Kafka topics to external systems. They consume records from Kafka topics and write them to the sink system, such as a database or data warehouse.

## Why Use Kafka Connectors?

- **Ease of Use**

  They abstract the complexity of data ingestion and distribution.

- **Scalability**

  Connectors can scale horizontally to handle large data volumes.

- **Reusability**

  Once configured, they can be reused across various Kafka implementations.

## What is Kafka Streams?

[Kafka Streams](https://kafka.apache.org/36/documentation/streams/core-concepts) is another key component of Kafka, designed for processing and analyzing data in real time. It is a client library for building applications and microservices where the input and output data are stored in Kafka topics.

It offers two primary abstractions for processing streaming data.

### KStream

KStream represents a stream of continuously updating data. A KStream is ideal for handling data that represents a series of individual records, like log lines or sensor readings.

### KTable

KTable represents a changelog stream of updates to a table. A KTable is useful for data that models more static data or the latest state of each key, such as user profiles or inventory levels.

## Using Kafka Streams with Connectors

Kafka Connectors function on a one-to-one basis, where each record in a Kafka topic corresponds to a record in a connected system. This characteristic often necessitates the use of Kafka Streams for more advanced data processing tasks. For instance, a source connector could ingest data into a Kafka topic, after which a Kafka Streams application processes this data. The processed data is then relayed to another topic and subsequently transferred to an external system using a sink connector.

### Example Workflow

Here’s a practical workflow to illustrate the synergy between Kafka Connectors and Kafka Streams:

1. **Ingestion**

    Data is ingested from a source system into a Kafka topic via a source connector.

2. **Processing**

    A Kafka Streams application reads from this topic. It uses KStream to filter and modify the data or KTable to maintain the latest state of each key. The processed data is then written to a new Kafka topic.

3. **Export**

    A sink connector takes this data from the new topic and exports it to an external system or database.

## Setting Things Up

A working example can be found on [GitHub](https://github.com/cqrs/connector-demo).

### Connect

1. **Deploying the Connect Worker**

    Before you dive into setting up a connector, you need to have a Kafka cluster up and running. [Redpanda](https://docs.redpanda.com/current/get-started/quick-start/) makes this especially easy with their single binary approach.

    Kafka Connect Workers and Kafka Streams applications must be deployed separately and configured to point to Kafka-compliant brokers. [Containerization](https://docs.redpanda.com/current/deploy/deployment-option/self-hosted/docker-image/) makes this fairly trivial.

2. **Choosing Connector Plugins**

    Determine what external system you want to connect with. For instance, if you want to pull data from a Postgres database, you would use a JDBC source connector. There is a [vast pool of plugins](https://docs.confluent.io/platform/current/connect/kafka_connectors.html) to choose from, and even more provided by individual vendors.

3. **Installing Connector Plugins**

    Once you've chosen a connector, you need to install it. You can [build an image](https://docs.redpanda.com/current/deploy/deployment-option/self-hosted/kubernetes/k-deploy-connectors/#add-the-connector-to-the-docker-image), and package up all the plugins. This can be tricky, as there seems to be more documentation for installing these in SaaS environments than there is for downloading source files; just keep digging. Once packaged and deployed, the plugins are accessible to the Connect Worker - they still need to be instantiated via an API call.

4. **Configuring Connector Plugins**

    Connectors are configured with properties files. These files specify details like the name of the connector, the connector class to use, the topic to source to or sink from, and connection details for the external system. Base configuration for connectors can be stored in the `CONNECT_CONFIGURATION` env of the Connect Worker itself, then each connector plugin has its own configuration. Individual connector config is typically stored in JSON format to be used in the instantiation API call.

5. **Starting Connector Plugins**

    It's worth noting that connectors are added to the cluster imperatively. With the Kafka environment running, you can instantiate connectors using the Kafka Connect REST API or the [Redpanda Console](https://github.com/redpanda-data/console).

6. **Monitoring and Managing Connector Plugins**

    Once the connector is up and running, you can monitor its performance and manage it through the Kafka Connect REST API or the [Redpanda Console](https://github.com/redpanda-data/console).

### Streams

1. **Configuring Streams Applications**

    While Kafka Connectors primarily rely on configuration settings, Kafka Streams require the development of actual code for processing data. The most extensive libraries for this purpose are typically found within the Java ecosystem, although there are various alternatives available. In Kafka Streams and similar stream processing frameworks, there are numerous common functions that enable the manipulation, transformation, and aggregation of streaming data. These functions include:

    - Filter

      This function is used to selectively include or exclude records based on specific conditions. It examines each record and decides whether to keep it or discard it based on a predicate (a boolean function).

    - Map

      Similar to the map function in functional programming, this operation applies a function to each record in the stream, transforming the record into a new form. The function can modify the record's key, value, or both.

    - FlatMap

      This function is a combination of map and flatten. It applies a function to each record, which transforms the record into zero or more new records. For example, you might use flatMap to split a single record into multiple records.

    - ForEach

      This action is used to perform a specific operation for each record in the stream, typically for side effects (such as logging or updating an external system). Unlike other functions, forEach does not return a new stream.

    - GroupByKey/GroupBy

      These functions are used to group records in the stream by their keys (or by values, or by a function of the key/value). This is often a preliminary step before performing aggregations.

    - Aggregate

      Used to perform rolling aggregation on grouped records. Common examples include sum, count, average, min, and max.

    - Join

      This operation allows you to combine records from two streams based on their keys. Joins can be inner, outer, left, or right, similar to joins in relational databases.

    - Window

      This function is used to apply operations on records within a specific time window. It's essential for time-sensitive calculations, such as moving averages or total counts within time intervals.

    - Reduce

      Similar to aggregate, reduce is used for combining records in a stream to produce a single aggregate result. It applies a function to two records at a time, reducing them to a single record.

    - Branch

      This function splits a stream into two or more streams based on specified predicates. Each resulting stream contains records that match its corresponding predicate.

2. **Deploying Streams Applications**

    These are deployed as an application on your stack, same as any other home grown application.

## Conclusion

Kafka Connectors are a powerful tool in the Kafka ecosystem, helping to seamlessly integrate Kafka with a variety of external systems. By combining Kafka Connectors with Kafka Streams, you can build powerful, scalable, and real-time data processing pipelines. This setup allows for more complex operations, which go beyond the capabilities of Connectors alone. Happy connecting!
