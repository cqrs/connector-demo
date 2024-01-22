# Connector Demo

Once our commands have been captured by the message bus ([Redpanda](https://redpanda.com/), which is a Kafka compatible streaming data platform), we need to process them. We'll be using Kafka Streams to process the incoming records, then Kafka Connect to sink records into storage. There is a broad ecosystem of [Connectors](https://docs.confluent.io/platform/current/connect/kafka_connectors.html) for [Kafka Connect](https://docs.confluent.io/platform/current/connect/index.html) that allow us to source records into, and sink records from, our message bus.

This demo captures commands of a person following another person:

```json
{
  "followerID": "1",
  "followerName": "Sue",
  "followeeID": "2",
  "followeeName": "Bob"
}
```

The data that we'll want to query is going to be:

- relational (postgres)

  each person, and their metadata (only name for now)

  ```sql
  SELECT * FROM people;
  ```

  1 command corresponds to 2 records in SQL.

- graph (neo4j)

  each person, and their relationships

  ```cql
  MATCH (person:Person)
  RETURN person
  ```

  1 command corresponds to 2 nodes and 1 relationship in Cypher.

Since each incoming command maps to N records in each sink, it's necessary to perform some ETL and stream the records to other topics. The pertinent code for this is in [stream](./stream).

Once the data is in the format that each of the Connectors expect, the sinks will simply do their thing, read from their assigned topics, and insert the data into their appropriate databases. There's no code for this, and only configuration in [connect](./connect).

## Usage

Start the services:

```sh
docker compose up -d
```

Create the topic(s) and add some records:

```sh
docker compose exec -it redpanda bash

rpk topic create events graph people
cat /opt/kafka/data/import.ndjson | rpk topic produce events
exit

# The stream service likely failed due to missing topics. Restart.
docker compose up -d
```

Call the Connector API to instantiate the sinks:

```sh
curl -X POST -H "Content-Type: application/json" --data @connect/graph-connector.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @connect/jdbc-connector.json http://localhost:8083/connectors
```

Check out the [Redpanda console](http://localhost:8080/)!
