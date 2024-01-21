package com.cqrs.demo.stream

import play.api.libs.json._
import org.apache.kafka.common.serialization.Serde

case class Relationship(
    op: String,
    `type`: String,
    rel_type: String,
    from: RelationshipNode,
    to: RelationshipNode
)

case class RelationshipNode(
    labels: Seq[String],
    ids: IDs
)

object Relationship {
  implicit val idsReads: Reads[IDs] = Json.reads[IDs]
  implicit val relationshipNodeReads: Reads[RelationshipNode] = Json.reads[RelationshipNode]
  implicit val relationshipReads: Reads[Relationship] = Json.reads[Relationship]
  implicit val idsWrites: Writes[IDs] = Json.writes[IDs]
  implicit val relationshipNodeWrites: Writes[RelationshipNode] = Json.writes[RelationshipNode]
  implicit val relationshipWrites: Writes[Relationship] = Json.writes[Relationship]

  val jsonSerde: Serde[Relationship] = SerDes.jsonSerde[Relationship]

  def forwards(event: Event): Seq[(String, Relationship)] = {
    val relationship = Relationship(
      op = "merge",
      `type` = "relationship",
      rel_type = "FOLLOWS",
      from = RelationshipNode(labels = Seq("Person"), ids = IDs(uuid = event.followerID)),
      to = RelationshipNode(labels = Seq("Person"), ids = IDs(uuid = event.followeeID))
    )

    Seq(
      (s"${event.followerID}${event.followeeID}", relationship)
    )
  }
}
