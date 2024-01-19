name := "ExampleConnector"

version := "0.1"

scalaVersion := "2.13.12"

// Resolvers for additional repositories if needed
resolvers ++= Seq(
  "confluent" at "https://packages.confluent.io/maven/",
)

// Library dependencies
libraryDependencies ++= Seq(
  "org.apache.kafka" % "connect-api" % "3.6.1",
  "org.apache.kafka" %% "kafka" % "3.6.1",
  "org.playframework" %% "play-json" % "3.0.1",
  // Add any additional dependencies here
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
