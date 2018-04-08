import org.openqa.selenium.By
import org.scalatest.{FeatureSpec, GivenWhenThen}
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerTest, ServerProvider}

class Cigarra3310Spec
    extends FeatureSpec
    with GivenWhenThen
    with OneBrowserPerTest
    with GuiceOneServerPerTest
    with HtmlUnitFactory
    with ServerProvider {

  feature("Cigarra3310") {
    scenario("As a master I want to create a new Cigarra") {
      Given("I navigate to the home page")
      val homePage = "http://localhost:" + port + "/"
      go to homePage

      And("I insert the new Cigarra name")
      val nameEditText = webDriver.findElement(By.id("name"))
      nameEditText.sendKeys("some-name")

      And("I click on the new Cigarra button")
      val newCigarraButton = webDriver.findElement(By.id("create"))
      clickOn(newCigarraButton)

      Then("I am redirected to the Editor page")
      currentUrl matches """.*/cigarra/.*/editor"""
    }
  }
}
