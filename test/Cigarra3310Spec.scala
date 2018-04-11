import org.openqa.selenium.By
import org.scalatest.{FeatureSpec, GivenWhenThen, MustMatchers}
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerTest, ServerProvider}

class Cigarra3310Spec
    extends FeatureSpec
    with GivenWhenThen
    with OneBrowserPerTest
    with GuiceOneServerPerTest
    with HtmlUnitFactory
    with ServerProvider
    with MustMatchers {

  feature("Cigarra3310") {
    scenario("As a master I want to create a new Cigarra") {
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

      And("I fill create a new Level")
      val description = webDriver.findElement(By.id("description"))
      val solution = webDriver.findElement(By.id("solution"))
      description.sendKeys("some-description")
      solution.sendKeys("some-solution")

      Then("the page is reloaded")
      description.getText mustEqual ""
      solution.getText mustEqual ""

      And("I create another new Level")
      description.sendKeys("some-description")
      solution.sendKeys("some-solution")

      And("I finish the Cicada creation")
      val finishButton = webDriver.findElement(By.id("finish"))
      clickOn(finishButton)

      Then("I see my Cicada public url")
      val cicadaUrl = webDriver.findElement(By.id("url"))
      cicadaUrl.getText must not equal ""
    }
  }
}
