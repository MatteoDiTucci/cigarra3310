package services

import javax.inject.Singleton

@Singleton
class IdGenerator {

  def id: String = java.util.UUID.randomUUID.toString
}
