package domain

import play.api.libs.json.Json

case class Cigarra(guid: Option[String] = None, name: String)

object Cigarra {
  implicit val format = Json.format[Cigarra]
}
