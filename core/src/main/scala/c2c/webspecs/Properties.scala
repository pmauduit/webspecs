package c2c.webspecs

import System.{getProperties,getenv}
import java.util.{Properties => JProperties}
import scalax.file.Path
import collection.JavaConverters._
import scalax.io.Resource
import util.control.Exception

object Properties {

  def TEST_TAG = "{automated_test_metadata}"
  def TEST_URL_KEY = "test.server"
  lazy val all:Map[String,String] = {
    def load(file:String):Map[String,String] = {
      val inputStream =
        if (Path(file).exists) Some(Path(file).inputStream)
        else Exception.catching(classOf[IllegalArgumentException]).opt(Resource.fromClasspath(file,getClass))
      inputStream map {
        resource =>
          resource.acquireAndGet{ in =>
            val p = new JProperties()
            p.load(in)
            var propMap = p.asScala.toMap.asInstanceOf[Map[String,String]]
            propMap.get("@includes") map { includes =>
              propMap -= "@includes"
              propMap ++= includes split "," flatMap {load(_)}
            }
            propMap.get("@override") map { overrides =>
              propMap -= "@override"
              propMap = (overrides split "," flatMap {load(_)}).toMap ++ propMap
            }
          propMap
          }
      } getOrElse Map[String,String]()
    }

    val sysProps = getProperties.asScala /// NEED to load sstem and env properties and add them all to the map
    val envProps = getenv.asScala /// NEED to load sstem and env properties and add them all to the map
    val props = load("config.properties")
    val allProps = props ++ envProps ++ sysProps
    resolveReferences(allProps)
  }
  private def resolveReferences(map:Map[String,String]) = {
    val Ref = """\$\{(.+?)\}""".r
    var found = true
    var updatedMap = map
    while(found) {
      found = false;
      updatedMap = updatedMap.map {
        case (key,value) if Ref.findFirstIn(value).nonEmpty =>
          found = true
          val newVal = Ref.replaceAllIn(value,{matcher =>
            map.get(matcher.group(1)) getOrElse(throw new IllegalArgumentException("No substitution for "+matcher.group(1)))
          })
          (key,newVal)
        case entry => entry
      }
    }
    updatedMap
  }

  def apply(key: String) = all.get(key)

  def get(key:String) = apply(key) getOrElse{
    throw new IllegalStateException(key+" is required to be defined.  Most likely it is expected to be a jvm option")
  }
}