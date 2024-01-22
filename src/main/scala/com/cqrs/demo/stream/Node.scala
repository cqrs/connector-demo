package com.cqrs.demo.stream

import play.api.libs.json._

case class Node(
    op: String,
    `type`: String,
    labels: Seq[String],
    ids: IDs,
    properties: NodeProperties
)
case class IDs(uuid: String)
case class NodeProperties(name: String)

object Node {
  implicit val idsReads: Reads[IDs] = Json.reads[IDs]
  implicit val nodePropertiesReads: Reads[NodeProperties] = Json.reads[NodeProperties]
  implicit val nodeReads: Reads[Node] = Json.reads[Node]
  implicit val idsWrites: Writes[IDs] = Json.writes[IDs]
  implicit val nodePropertiesWrites: Writes[NodeProperties] = Json.writes[NodeProperties]
  implicit val nodeWrites: Writes[Node] = Json.writes[Node]

  def forwards(event: Event): Seq[(String, Node)] = {
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

    Seq(
      (event.followerID, node1),
      (event.followeeID, node2)
    )
  }
}
