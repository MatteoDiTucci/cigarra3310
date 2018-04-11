package domain

import java.util.UUID

case class Level(guid: Option[UUID] = None, description: String, solution: String)
