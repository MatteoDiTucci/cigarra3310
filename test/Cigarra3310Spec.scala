import org.openqa.selenium.By
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
        val title = By.id("title")
        val enigmaDescription = By.id("description")
        val enigmaSolution = By.id("solution")
        val filePickerLabel = By.id("fileLabel")
        val filePickerButton = By.id("fileInput")

        webDriver.findElement(title)
        webDriver.findElement(enigmaDescription)
        webDriver.findElement(enigmaSolution)
        webDriver.findElement(filePickerLabel)
        webDriver.findElement(filePickerButton)
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
