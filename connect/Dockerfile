FROM eclipse-temurin:17-jre AS builder

ARG NEO4J_CONNECTOR_VERSION=5.0.3
ARG GUAVA_VERSION=32.1.2-jre
ARG JDBC_VERSION=10.7.4
ARG JDBC_DRIVER_VERSION=42.7.1

RUN apt-get update && apt-get install -y wget unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /opt/kafka/connect-plugins

# Download and extract the Kafka Connect Neo4j Connector
RUN wget -nv -O /tmp/neo4j-connector.zip https://github.com/neo4j-contrib/neo4j-streams/releases/download/$NEO4J_CONNECTOR_VERSION/neo4j-kafka-connect-neo4j-$NEO4J_CONNECTOR_VERSION.zip && \
    unzip /tmp/neo4j-connector.zip && \
    rm /tmp/neo4j-connector.zip
# Download Guava, as the dependency is oddly missing from the release.
RUN wget -nv -P ./neo4j-kafka-connect-neo4j-$NEO4J_CONNECTOR_VERSION/lib https://repo1.maven.org/maven2/com/google/guava/guava/$GUAVA_VERSION/guava-$GUAVA_VERSION.jar

# Download and extract the Kafka Connect JDBC Connector
RUN wget -nv -P ./kafka-connect-jdbc-$JDBC_VERSION/lib https://packages.confluent.io/maven/io/confluent/kafka-connect-jdbc/$JDBC_VERSION/kafka-connect-jdbc-$JDBC_VERSION.jar
RUN wget -nv -P ./kafka-connect-jdbc-$JDBC_VERSION/lib https://jdbc.postgresql.org/download/postgresql-$JDBC_DRIVER_VERSION.jar

FROM redpandadata/connectors:v1.0.13

# Import the compiled binaries from the first stage.
COPY --from=builder /opt/kafka/connect-plugins/ /opt/kafka/connect-plugins/
