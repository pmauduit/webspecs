package c2c.webspecs
package geonetwork
package geocat

import java.util.UUID
import c2c.webspecs.geonetwork.GeonetConfig
import c2c.webspecs.ExecutionContext
import scala.xml.Node

/**
 * Fixtures that only apply in Geocat
 */
object GeocatFixture {
  def format = new Fixture[GeonetConfig] {
    val name = "WebSpecs"
    val version = UUID.randomUUID().toString
    private var _id:Int = -1

    def id = _id

    def delete(config: GeonetConfig, context: ExecutionContext) =
      (config.adminLogin then DeleteFormat.setIn(id))(None)(context)

    def create(config: GeonetConfig, context: ExecutionContext) = {
      val formats = (config.adminLogin then AddFormat(name, version) then ListFormats.setIn(name))(None)(context)
      _id = formats.value.find(_.version == version).get.id
    }
  }
  
  def reusableExtent(extentXml:Node) = new Fixture[GeonetConfig] {
    private var _id:String = _
    def id = _id
    
    def delete(config: GeonetConfig, context: ExecutionContext) =
      (config.adminLogin then DeleteExtent(Extents.NonValidated,id))(None)(context)

    def create(config: GeonetConfig, context: ExecutionContext) = {
      val extents = (config.adminLogin then ProcessSharedObject(extentXml, true))(None)(context)
      val xml = extents.value
      _id = XLink.id(extents.value \\ "extent" head).get
    }
  }
}