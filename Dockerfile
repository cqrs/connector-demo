FROM eclipse-temurin:17-jre AS builder

ARG SCALA_VERSION=2.13.12
ARG SBT_VERSION=1.9.8
ARG NEO4J_CONNECTOR_VERSION=5.0.3
ARG GUAVA_VERSION=32.1.2-jre

RUN apt-get update && apt-get install -y wget unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Install Scala
RUN wget -nv https://downloads.lightbend.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz && \
    tar xvf scala-$SCALA_VERSION.tgz && \
    mv scala-$SCALA_VERSION /usr/share/scala && \
    ln -s /usr/share/scala/bin/* /usr/bin/ && \
    rm scala-$SCALA_VERSION.tgz

# Install sbt
RUN wget -nv https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz && \
    tar -xzf sbt-$SBT_VERSION.tgz -C /usr/local --strip-components=1 && \
    rm sbt-$SBT_VERSION.tgz

# Be sure this aligns with CONNECT_PLUGIN_PATH
WORKDIR /opt/kafka/connect-plugins

# Download and extract the Kafka Connect Neo4j Connector
RUN wget -nv -O /tmp/neo4j-connector.zip https://github.com/neo4j-contrib/neo4j-streams/releases/download/$NEO4J_CONNECTOR_VERSION/neo4j-kafka-connect-neo4j-$NEO4J_CONNECTOR_VERSION.zip && \
    unzip /tmp/neo4j-connector.zip && \
    rm /tmp/neo4j-connector.zip

# Download Guava, as the dependency is oddly missing from the release.
RUN wget -P ./neo4j-kafka-connect-neo4j-$NEO4J_CONNECTOR_VERSION/lib https://repo1.maven.org/maven2/com/google/guava/guava/$GUAVA_VERSION/guava-$GUAVA_VERSION.jar

WORKDIR /usr/src/app

COPY . .

RUN sbt clean assembly

FROM redpandadata/connectors:v1.0.13

# Import the compiled binaries from the first stage.
COPY --from=builder /usr/src/app/target/scala-*/*.jar /opt/kafka/connect-plugins/example-connector/
COPY --from=builder /opt/kafka/connect-plugins/ /opt/kafka/connect-plugins/
