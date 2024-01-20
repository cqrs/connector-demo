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

and inserts them in neo4j and postgres.

## Usage

```sh
docker compose up -d
```

Then exec into the redpanda instance to create the topic that we'll be using.

```sh
docker compose exec -it redpanda bash

rpk topic create events people graph
```

Then from your local host

```sh
curl -X POST -H "Content-Type: application/json" --data @neo4j-connector.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @jdbc-connector.json http://localhost:8083/connectors
```
