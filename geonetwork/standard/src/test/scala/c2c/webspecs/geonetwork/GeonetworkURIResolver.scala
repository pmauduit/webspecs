package c2c.webspecs
package geonetwork

class GeonetworkURIResolver extends UriResolver {
  def apply(uri: String, params: Seq[(String, String)]) = {
    val baseURL = Properties.testServer
    val locale = Properties(GeonetConfig.LOCALE_KEY) getOrElse "eng"

    val service =
      if (!uri.startsWith(XLink.PROTOCOL)) uri
      else "http:/" :: baseURL :: "geonetwork/srv" :: locale :: uri.drop(XLink.PROTOCOL.size) :: Nil mkString "/"

    val segments = service.split("/")
    if(segments.size > 2){
      val sep = if (service contains "?") "&" else "?"
      val paramString = params.map { e => e._1 + "=" + e._2 }.mkString("&")
      service + sep + paramString
    } else {
      val serviceUrl = if(segments.size == 2) "http:/" :: baseURL :: "geonetwork/srv" :: segments(0) :: segments(1) :: Nil mkString "/"
    		  		   else "http:/" :: baseURL :: "geonetwork/srv" :: locale :: service :: Nil mkString "/"

      if (params.isEmpty) {
        serviceUrl
      } else {
        val paramString = params.map {
          case (key, value) => key + "=" + value
        } mkString ("?", "&", "")
        serviceUrl + paramString
      }
    }
  }
}