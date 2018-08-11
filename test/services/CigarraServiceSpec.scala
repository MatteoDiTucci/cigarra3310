package services

import domain.{Cigarra, Level}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import repositories.{CigarraRepository, LevelRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class CigarraServiceSpec extends WordSpec with MustMatchers with MockitoSugar {

  private val cigarraId = "some-cigarra-id"
  private val levelId = "level-id"

  "CigarraService" when {
    "creating a new Cigarra" should {

      "return the new Cigarra id" in {
        val idGenerator = mock[IdGenerator]
        mockIdGeneratorWithId(cigarraId, idGenerator)
        val cigarraRepository = mockSuccessfulCigarraSave
        val service = createService(cigarraRepository = cigarraRepository, idGenerator = idGenerator)

        service.createCigarraWithName("some-name") mustBe cigarraId
        verify(cigarraRepository, times(1)).save(any[String], any[String])
      }
    }

    "retrieving an existing Cigarra by its id" should {

      "return the Cigarra" in {
        val cigarra = Cigarra(cigarraId, "some-name")
        val cigarraRepository = mock[CigarraRepository]
        when(cigarraRepository.findCigarra(any[String])).thenReturn(Future.successful(cigarra))
        val service = createService(cigarraRepository)

        Await.result(service.findCigarra(cigarraId), 1.second) mustBe cigarra
      }
    }

    "retrieving the first Level of a Cigarra by its id" should {

      "return the first level of a Cigarra" in {
        val level = Level(levelId, "some-description", "some-solution")
        val cigarraRepository = mock[CigarraRepository]
        val levelRepository = mock[LevelRepository]
        mockStoredLevel(level, levelRepository)
        mockCigarraFirstLevelWithId(level.id, cigarraRepository)
        val service = createService(cigarraRepository, levelRepository)

        Await.result(service.findFirstLevel(cigarraId), 1.second) mustBe Some(level)
      }
    }

    "linking the cigarra to its first level" when {

      "the cigarra has no level" should {
        "check if the cigarra has no level and then link it to the new level" in {
          val cigarraRepository = mock[CigarraRepository]
          mockCigarraWithoutLevels(cigarraRepository)
          mockSuccessfullySetFirstLevel(cigarraRepository)

          val service = createService(cigarraRepository)
          Await.result(service.setFirstLevel(cigarraId, levelId), 1.second)

          verify(cigarraRepository, times(1)).findFirstLevel(cigarraId)
          verify(cigarraRepository, times(1)).setFirstLevel(cigarraId, levelId)
        }
      }

      "the Cigarra already has at least one level" should {
        "check if the Cigarra does not have a first level" in {
          val cigarraRepository = mock[CigarraRepository]
          when(cigarraRepository.findFirstLevel(cigarraId)).thenReturn(Future.successful(Some(levelId)))

          val service = createService(cigarraRepository)
          Await.result(service.setFirstLevel(cigarraId, levelId), 1.second)

          verify(cigarraRepository, times(1)).findFirstLevel(cigarraId)
          verify(cigarraRepository, never()).setFirstLevel(cigarraId, levelId)
        }
      }
    }
  }

  private def mockSuccessfullySetFirstLevel(cigarraRepository: CigarraRepository) =
    when(cigarraRepository.setFirstLevel(cigarraId, levelId)).thenReturn(Future.successful(true))
  private def mockCigarraWithoutLevels(cigarraRepository: CigarraRepository) =
    when(cigarraRepository.findFirstLevel(cigarraId)).thenReturn(Future.successful(None))
  private def mockStoredLevel(level: Level, levelRepository: LevelRepository) =
    when(levelRepository.find(levelId)).thenReturn(Future.successful(level))
  private def mockCigarraFirstLevelWithId(levelId: String, cigarraRepository: CigarraRepository) =
    when(cigarraRepository.findFirstLevel(cigarraId)).thenReturn(Future.successful(Some(levelId)))
  private def mockSuccessfulCigarraSave = {
    val cigarraRepository = mock[CigarraRepository]
    when(cigarraRepository.save(any[String], any[String])).thenReturn(Future.successful(true))
    cigarraRepository
  }
  private def mockIdGeneratorWithId(id: String, idGenerator: IdGenerator) =
    when(idGenerator.id).thenReturn(id)
  private def createService(cigarraRepository: CigarraRepository,
                            levelRepository: LevelRepository = mock[LevelRepository],
                            idGenerator: IdGenerator = mock[IdGenerator]) =
    new CigarraService(cigarraRepository, levelRepository, idGenerator)
}
