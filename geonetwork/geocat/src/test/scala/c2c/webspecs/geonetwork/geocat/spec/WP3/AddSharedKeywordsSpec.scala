package c2c.webspecs
package geonetwork
package geocat
package spec.WP3

import org.specs2._
import specification._
import java.util.UUID
import scala.xml.NodeSeq
import geocat.GeocatConstants.NON_VALIDATED_THESAURUS
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AddSharedKeywordsSpec extends GeonetworkSpecification { def is =
  "This specification tests creating shared keyword by passing in a keyword xml snippet"                        ^ Step(setup) ^ t ^
    "Calling shared.process with the xml snippet for adding a keyword"                                          ^ keywordAdd(deValue).toGiven ^
    "Should have 200 result"                                                                                    ^ a200ResponseThen.narrow[Response[NodeSeq]] ^
    "Keyword node should have an xlink href"                                                                    ^ hrefInElement.toThen ^
    "xlink href should retrieve the full keyword"                                                               ^ xlinkGetElement.toThen ^
    "Will result in a new shared keyword"                                                                       ! onlyKeywordInstance ^
                                                                                                                  endp ^
    "Adding same keyword should return same xlink"										                        ^ Step(keywordAdd(newDeValue)) ^
      "and the xlink should return same keyword"											                    ! onlyKeywordInstance ^
                                                                                                                  endp^
    "Updating an existing keyword with new XML which has a new de translation"		                             ^ updateKeyword.toGiven ^
      "Should have 200 result"                                                                                   ^ a200ResponseThen.narrow[Response[IsoKeyword]] ^
      "must result in the keyword retrieved from the xlink also having the new translation"                      ^ hasNewTranslation.toThen ^
                                                                                                                  endp^
    "Deleted new keyword"                                                                                        ^ Step(deleteNewKeyword) ^
    "Must correctly delete said keyword"                                                                         ! noKeyword
                                                                                                                  Step(tearDown)

  def keywordAdd(deValue:String) = () => (config.adminLogin then ProcessSharedObject(keywordXML(deValue)))(None)
  val hrefInElement = (result:Response[NodeSeq]) => (result.value \\ "descriptiveKeywords" \@ "xlink:href") must not beEmpty
  val xlinkGetElement = (result:Response[NodeSeq]) => {
    val href = (result.value \\ "descriptiveKeywords" \@ "xlink:href")(0)
    val xlink = GetRequest(href)(None)
    (xlink must haveA200ResponseCode) and
      haveCorrectTranslation (xlink, "#DE", deValue) and
      haveCorrectTranslation (xlink, "#EN", enValue) and
      haveCorrectTranslation (xlink, "#FR", frValue)

  }

  def haveCorrectTranslation(xlink:Response[XmlValue], locale:String,expected:String) =
    xlink.value.withXml{xml =>
      val nodeName = "LocalisedCharacterString"
      val repr = (xml \\ nodeName)+" @ "+locale
      xml \\ nodeName find {_ @@ "locale" == Some(locale)} map (_.text) aka repr must beSome(expected)}

  def Search = SearchKeywords(List(NON_VALIDATED_THESAURUS))
  def onlyKeywordInstance = Search(deValue).value.filter(_.value == deValue) must haveSize(1)

  val updateKeyword = () => {
    val response = (UpdateSharedObject(keywordXML(newDeValue)) startTrackingThen Search.setIn(frValue))(None)
    assert(response.last.value.size == 1, "Expected a single keyword with "+frValue)
    val keyword = response.last.value.head
    val isoKeyword = GetIsoKeyword(NON_VALIDATED_THESAURUS,List("de","en","fr"))(keyword.encodedURI)
    response._1.map(_ => isoKeyword.value)
  }
  
  val hasNewTranslation = (resp:Response[IsoKeyword]) => 
    	(resp.value.label("DE") aka "german translation" must_== newDeValue) and 
    		(resp.value.label("FR") aka "french translation" must_== frValue) and
    		(resp.value.label("EN") aka "english translation" must_== enValue)
  
  def deleteNewKeyword = Search(frValue).value.foreach{c => DeleteKeyword(NON_VALIDATED_THESAURUS,"",c.id)}
  def noKeyword = Search(frValue).value must beEmpty
  lazy val uuid = UUID.randomUUID().toString
  lazy val deValue = uuid+"de*automated*"
  lazy val newDeValue = uuid+"NewDe*automated*"
  lazy val enValue = uuid+"en*automated*"
  lazy val frValue = uuid+"fr*automated*"
  def keywordXML(deValue:String) =
    <gmd:descriptiveKeywords  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd">
        <gmd:MD_Keywords>
          <gmd:keyword xsi:type="gmd:PT_FreeText_PropertyType">
            <gmd:PT_FreeText>
              <gmd:textGroup>
                <gmd:LocalisedCharacterString locale="#DE">{deValue}</gmd:LocalisedCharacterString>
              </gmd:textGroup>
              <gmd:textGroup>
                <gmd:LocalisedCharacterString locale="#EN">{enValue}</gmd:LocalisedCharacterString>
              </gmd:textGroup>
              <gmd:textGroup>
                <gmd:LocalisedCharacterString locale="#FR">{frValue}</gmd:LocalisedCharacterString>
              </gmd:textGroup>
            </gmd:PT_FreeText>
          </gmd:keyword>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>

}