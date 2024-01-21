# Connector Demo

Once our commands have been captured by Apache Kafka, we need to process them and put them into storage. There is a broad ecosystem of [Connectors](https://docs.confluent.io/platform/current/connect/kafka_connectors.html) for [Kafka Connect](https://docs.confluent.io/platform/current/connect/index.html).

This demo takes events that simulate the act of a person following another person

```json
{
  "followerID":"1",
  "followerName":"Sue",
  "followeeID":"2",
  "followeeName":"Bob"
}
```

and sinks them into the following:

- neo4j
- postgres

Since each event maps to N records in our sinks, it's necessary to stream records to other topics.

## Usage

```sh
docker compose up -d
```

Create the topic(s).

```sh
docker compose exec -it redpanda bash

rpk topic create events graph jdbc
```

Call the connector API to instantiate the desired sinks.

```sh
curl -X POST -H "Content-Type: application/json" --data @connect/graph-connector.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @connect/jdbc-connector.json http://localhost:8083/connectors
```

Data can be loaded into Kafka via

```sh
docker compose exec -it redpanda bash

cat /opt/kafka/data/import.ndjson | rpk topic produce events
```
