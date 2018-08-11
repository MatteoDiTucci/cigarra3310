package domain

import play.api.libs.json.Json

case class Cigarra(id: String, name: String)

object Cigarra {
  implicit val format = Json.format[Cigarra]
}
