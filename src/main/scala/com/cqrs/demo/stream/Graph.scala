package com.cqrs.demo.stream

import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.Serdes._
import play.api.libs.json.{Json, Writes}

case class Node(
    op: String,
    `type`: String,
    labels: Seq[String],
    ids: IDs,
    properties: NodeProperties
)
case class IDs(uuid: String)
case class NodeProperties(name: String)

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

object Graph {
  implicit val idsWrites: Writes[IDs] = Json.writes[IDs]
  implicit val nodePropertiesWrites: Writes[NodeProperties] = Json.writes[NodeProperties]
  implicit val nodeWrites: Writes[Node] = Json.writes[Node]
  implicit val relationshipNodeWrites: Writes[RelationshipNode] = Json.writes[RelationshipNode]
  implicit val relationshipWrites: Writes[Relationship] = Json.writes[Relationship]

  def forwards(event: Event): Seq[(String, String)] = {
    val node1 = Node(
      op = "merge",
      `type` = "node",
      labels = Seq("Person"),
      ids = IDs(uuid = event.followerID),
      properties = NodeProperties(name = event.followerName)
    )
    val node2 = Node(
      op = "merge",
      `type` = "node",
      labels = Seq("Person"),
      ids = IDs(uuid = event.followeeID),
      properties = NodeProperties(name = event.followeeName)
    )
    val relationship = Relationship(
      op = "merge",
      `type` = "relationship",
      rel_type = "FOLLOWS",
      from = RelationshipNode(labels = Seq("Person"), ids = IDs(uuid = event.followerID)),
      to = RelationshipNode(labels = Seq("Person"), ids = IDs(uuid = event.followeeID))
    )

    Seq(
      (event.followerID, Json.toJson(node1).toString),
      (event.followeeID, Json.toJson(node2).toString),
      (s"${event.followerID}${event.followeeID}", Json.toJson(relationship).toString)
    )
  }
}
