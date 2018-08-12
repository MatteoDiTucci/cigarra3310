package dao

import com.typesafe.config.{Config, ConfigFactory}
import play.api.db.{Database, Databases}

object DbFixtures {

  def withMyDatabase[T](block: Database => T): T = {
    val config: Config = ConfigFactory.load("test.conf")
    Databases.withDatabase(
      driver = config.getString("db.default.driver"),
      url = config.getString("db.default.url"),
      config = Map(
        "username" -> config.getString("db.default.username"),
        "password" -> config.getString("db.default.password")
      )
    )(block)
  }
}
