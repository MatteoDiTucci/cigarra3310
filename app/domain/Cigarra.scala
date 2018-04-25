package domain

import play.api.libs.json.Json

case class Cigarra(guid: String, name: String)

object Cigarra {
  implicit val format = Json.format[Cigarra]
}
