package services

import javax.inject.Singleton

@Singleton
class UuidGenerator {

  def guid: String = java.util.UUID.randomUUID.toString
}
