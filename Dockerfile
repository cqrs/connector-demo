FROM eclipse-temurin:17-jre AS builder

ARG SCALA_VERSION=2.13.12
ARG SBT_VERSION=1.9.8
ARG NEO4J_CONNECTOR_VERSION=5.0.3
ARG GUAVA_VERSION=32.1.2-jre
ARG JDBC_VERSION=10.7.4

RUN apt-get update && apt-get install -y wget unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Install Scala
RUN wget -nv https://downloads.lightbend.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz && \
    tar -xvf scala-$SCALA_VERSION.tgz && \
    mv scala-$SCALA_VERSION /usr/share/scala && \
    ln -s /usr/share/scala/bin/* /usr/bin/ && \
    rm scala-$SCALA_VERSION.tgz

# Install sbt
RUN wget -nv https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz && \
    tar -xzf sbt-$SBT_VERSION.tgz -C /usr/local --strip-components=1 && \
    rm sbt-$SBT_VERSION.tgz

WORKDIR /opt/kafka/redpanda-plugins

# Download and extract the Kafka Connect Neo4j Connector
RUN wget -nv -O /tmp/neo4j-connector.zip https://github.com/neo4j-contrib/neo4j-streams/releases/download/$NEO4J_CONNECTOR_VERSION/neo4j-kafka-connect-neo4j-$NEO4J_CONNECTOR_VERSION.zip && \
    unzip /tmp/neo4j-connector.zip && \
    rm /tmp/neo4j-connector.zip
# Download Guava, as the dependency is oddly missing from the release.
RUN wget -P ./neo4j-kafka-connect-neo4j-$NEO4J_CONNECTOR_VERSION/lib https://repo1.maven.org/maven2/com/google/guava/guava/$GUAVA_VERSION/guava-$GUAVA_VERSION.jar

# Download and extract the Kafka Connect JDBC Connector
RUN wget -nv https://github.com/confluentinc/kafka-connect-jdbc/archive/refs/tags/v$JDBC_VERSION.tar.gz && \
    tar -xzvf v$JDBC_VERSION.tar.gz && \
    rm v$JDBC_VERSION.tar.gz

WORKDIR /usr/src/app

COPY . .

RUN sbt clean assembly

FROM redpandadata/connectors:v1.0.13

# Import the compiled binaries from the first stage.
COPY --from=builder /usr/src/app/target/scala-*/*.jar /opt/kafka/redpanda-plugins/connector-demo/
COPY --from=builder /opt/kafka/redpanda-plugins/ /opt/kafka/redpanda-plugins/
