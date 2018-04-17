import org.openqa.selenium.By
import org.scalatest.{FeatureSpec, GivenWhenThen, MustMatchers}
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerTest, ServerProvider}

class Cigarra3310Spec
    extends FeatureSpec
    with GivenWhenThen
    with GuiceOneServerPerTest
    with HtmlUnitFactory
    with ServerProvider
    with MustMatchers
    with OneBrowserPerTest {

  feature("Cigarra3310") {
    scenario("As a master I want to create a Cigarra") {
      Given("I navigate to the home page")
      val homePage = "http://localhost:" + port + "/"
      go to homePage

      And("I insert the new Cigarra name")
      val nameEditText = webDriver.findElement(By.id("name"))
      val cigarraName = "some-name"
      nameEditText.sendKeys(cigarraName)

      And("I click on the new Cigarra button")
      val newCigarraButton = webDriver.findElement(By.id("create"))
      clickOn(newCigarraButton)

      Then("I am redirected to the Editor page")
      val pageTitle = webDriver.findElement(By.id("name"))
      currentUrl matches """.*/cigarra/.*/editor"""
      pageTitle.getText mustEqual cigarraName

      And("I fill description and solution of a new Level")
      val firstLevelDescription = webDriver.findElement(By.id("description"))
      val firstLevelSolution = webDriver.findElement(By.id("solution"))
      firstLevelDescription.sendKeys("first-level-description")
      firstLevelSolution.sendKeys("first-level-solution")

      And("I click on the add level button")
      val addFirstLevelButton = webDriver.findElement(By.id("add-level"))
      clickOn(addFirstLevelButton)

      Then("the page is reloaded")
      val secondLevelDescription = webDriver.findElement(By.id("description"))
      val secondLevelSolution = webDriver.findElement(By.id("solution"))
      secondLevelDescription.getAttribute("value") mustEqual ""
      secondLevelSolution.getAttribute("value") mustEqual ""

      And("I create another new Level")
      secondLevelDescription.sendKeys("second-level-description")
      secondLevelSolution.sendKeys("second-level-solution")

      And("I click on the add level button")
      val addSecondLevelButton = webDriver.findElement(By.id("add-level"))
      clickOn(addSecondLevelButton)

      And("I finish the Cicada creation")
      val finishButton = webDriver.findElement(By.id("finish"))
      clickOn(finishButton)

      Then("I see my Cicada public url")
      val cicadaUrl = webDriver.findElement(By.id("url"))
      cicadaUrl.getText must not equal ""
    }

    scenario("As a Player I want to play a Cigarra") {
      Given("A Cigarra was created")
      val homePage = "http://localhost:" + port + "/"
      go to homePage

      val nameEditText = webDriver.findElement(By.id("name"))
      val cigarraName = "some-name"
      nameEditText.sendKeys(cigarraName)

      val newCigarraButton = webDriver.findElement(By.id("create"))
      clickOn(newCigarraButton)

      val firstLevelDescriptionEditor = webDriver.findElement(By.id("description"))
      val firstLevelSolutionEditor = webDriver.findElement(By.id("solution"))
      firstLevelDescriptionEditor.sendKeys("first level description")
      firstLevelSolutionEditor.sendKeys("first level solution")

      val addFirstLevelButton = webDriver.findElement(By.id("add-level"))
      clickOn(addFirstLevelButton)

      val finishButton = webDriver.findElement(By.id("finish"))
      clickOn(finishButton)

      val cicadaLink = webDriver.findElement(By.id("url"))

      And("I navigate to the url for a Cigarra")
      clickOn(cicadaLink)

      And("I fill in the solution for the first level")
      val firstLevelSolution = webDriver.findElement(By.id("solution"))
      firstLevelSolution.sendKeys("first level solution")

      val submitSolutionButton = webDriver.findElement(By.id("submit"))
      clickOn(submitSolutionButton)

      Then("I am redirected to the next level")
      !(currentUrl contains "/cigarra/some-cigarra-guid/level/some-level-guid") && (currentUrl contains "/cigarra/some-cigarra-guid/level/")
    }
  }
}
