import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.selenium.WebBrowser
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerTest

/**
 * Runs a browser test using Fluentium against a play application on a server port.
 */
class Cigarra3310Spec extends PlaySpec
  with OneBrowserPerTest
  with GuiceOneServerPerTest
  with HtmlUnitFactory
  with ServerProvider{

  "Cigarra3310" when {
    "navigating to Master editor page" should {
      "show Master dashboard" in {

        go to ("http://localhost:" + port + "/editor")

        textField("description").isDisplayed
        textField("solution").isDisplayed
        webDriver.findElement(By.id("continue"))
      }
    }

    "navigating to the Home Page" should {
      "show Cigarra3310 logo" in {

        go to ("http://localhost:" + port + "/")

        pageSource contains "Cigarra3310"
      }

    }
  }
}
