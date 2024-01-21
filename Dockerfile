FROM eclipse-temurin:17-jre AS builder

ARG SCALA_VERSION=2.13.12
ARG SBT_VERSION=1.9.8

RUN apt-get update && apt-get install -y wget && \
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

WORKDIR /usr/src/app

COPY . .

RUN sbt clean assembly

FROM eclipse-temurin:17-jre

WORKDIR /usr/src/app

COPY --from=builder /usr/src/app/target/scala-*/*.jar main.jar

CMD ["java", "-jar", "main.jar"]
