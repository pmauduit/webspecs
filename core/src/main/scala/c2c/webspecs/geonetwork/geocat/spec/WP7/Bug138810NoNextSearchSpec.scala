package c2c.webspecs
package geonetwork
package geocat
package spec
package WP7

import csw._
import org.specs2._
import matcher.ThrownExpectations
import specification.Step
import Thread._
import org.openqa.selenium.WebDriverBackedSelenium
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Bug138810NoNextSearchSpec extends GeocatSeleniumSpecification with ThrownExpectations { 

  def isImpl = 
  "Bug fix for 138810".title ^ 
    "Import 11 copies of a particular MD" ^ Step(importMd) ^
    "Verify there are 11 results from the search for those MD" ! correctResults ^
    "This spec tests searching for a uuid in the title in each language"        ! scala_specs2_1^
    "login"                                                                     ! scala_specs2_2^
    "perform a search for Titled"+uuid            ! scala_specs2_3^
    "assert there are 11 results and results 1 - 10 are showing"                ! scala_specs2_4^
    "assert there is a next button"                                             ! scala_specs2_5^
    "check that the results are displayed"                                      ! scala_specs2_6^
    "switch to french language"                                                 ! scala_specs2_7^
    "perform a search for Titled"+uuid            ! scala_specs2_8^
    "assert there are 11 results and results 1 - 10 are showing"                ! scala_specs2_9^
    "assert there is a next button"                                             ! scala_specs2_10^
    "check that the results are displayed"                                      ! scala_specs2_11^
    "switch to german"                                                          ! scala_specs2_12^
    "perform a search for Titled"+uuid            ! scala_specs2_13^
    "assert there are 11 results and results 1 - 10 are showing"                ! scala_specs2_14^
    "assert there is a next button"                                             ! scala_specs2_15^
    "check that the results are displayed"                                      ! scala_specs2_16^
    "Log back out"                                                              ! scala_specs2_17

  def importMd = {
    val importRequest = ImportMetadata.defaults(uuid,"/geocat/data/bare.iso19139.che.xml",false,getClass,ImportStyleSheets.NONE)._2
    
    1 to 11 foreach {_ => registerNewMd(Id(importRequest().value.id))}
  }

  def correctResults = {
    val xml = CswGetRecordsRequest(PropertyIsEqualTo("AnyText","Title"+uuid).xml)().value.getXml
    
    (xml \\ "@numberOfRecordsMatched").text.toInt must_== 11
  }
  
  def scala_specs2_1 = {
    import selenium._
    open("/geonetwork/srv/eng/geocat")
    success
  }

  def scala_specs2_2 = {
    import selenium._
    
    doWait(isElementPresent("id=username"))
    `type`("id=username", adminUser)
    `type`("id=password", adminPass)
    click("id=loginButton")
    waitForPageToLoad("30000")
    success
  }

  def scala_specs2_3 = {
    import selenium._
    `type`("id=anyField", "Title"+uuid)
    click("id=ext-gen53")
    doWait(isElementPresent("link=EN Title"+uuid))
    success
  }

  def scala_specs2_4 = {
    import selenium._
    isTextPresent("1-10 / 11") must beTrue
  }

  def scala_specs2_5 = {
    import selenium._
    isElementPresent("id=gotoNextPageButton") must beTrue
  }

  def scala_specs2_6 = {
    import selenium._
    isElementPresent("link=EN Title"+uuid) must beTrue
  }

  def scala_specs2_7 = {
    import selenium._
    click("link=Français")
    waitForPageToLoad("30000")
    success
  }

  def scala_specs2_8 = {
    import selenium._
    `type`("id=anyField", "Title"+uuid)
    click("id=ext-gen53")
    doWait(isElementPresent("link=FR Title"+uuid))
    success
  }

  def scala_specs2_9 = {
    import selenium._
    isTextPresent("1-10 / 11") must beTrue
  }

  def scala_specs2_10 = {
    import selenium._
    isElementPresent("id=gotoNextPageButton") must beTrue
  }

  def scala_specs2_11 = {
    import selenium._
    isElementPresent("link=FR Title"+uuid) must beTrue
  }

  def scala_specs2_12 = {
    import selenium._
    click("link=Deutsch")
    waitForPageToLoad("30000")
    success
  }

  def scala_specs2_13 = {
    import selenium._
    `type`("id=anyField", "Title"+uuid)
    click("css=td.x-btn-center")
    doWait(isElementPresent("link=DE Title"+uuid))
    success
  }

  def scala_specs2_14 = {
    import selenium._
    isTextPresent("1-10 / 11") must beTrue
  }

  def scala_specs2_15 = {
    import selenium._
    isElementPresent("id=gotoNextPageButton") must beTrue
  }

  def scala_specs2_16 = {
    import selenium._
    isElementPresent("link=DE Title"+uuid) must beTrue
  }

  def scala_specs2_17 = {
    import selenium._
    click("id=logoutButton")
    waitForPageToLoad("30000")
    success
  }

  val TIMEOUT = 30
  private def doWait(assertion: => Boolean) = 
    (1 to TIMEOUT).view map {_=> sleep(1000)} find { _ => assertion }

}