name := "ConnectorDemo"

version := "0.1"

scalaVersion := "2.13.12"

// Resolvers for additional repositories if needed
resolvers ++= Seq(
  "confluent" at "https://packages.confluent.io/maven/"
)

// Library dependencies
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.slf4j" % "slf4j-api" % "1.7.30",
  "org.apache.kafka" % "connect-api" % "3.6.1",
  "org.apache.kafka" % "kafka-clients" % "3.6.1",
  "io.confluent" % "kafka-json-schema-serializer" % "7.5.1",
  "org.apache.kafka" %% "kafka" % "3.6.1",
  "org.playframework" %% "play-json" % "3.0.1",
  "org.apache.kafka" %% "kafka-streams-scala" % "3.6.1"
)

// scalacOptions := Seq("-unchecked", "-deprecation")

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
  case PathList("META-INF", xs @ _*)             => MergeStrategy.discard
  case x                                         => MergeStrategy.first
}
