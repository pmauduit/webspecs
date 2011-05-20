package c2c.webspecs

import util.control.Exception._
import org.apache.http.{Header, HttpResponse}
import xml.NodeSeq

trait Response[+A] {
  def value:A
  def basicValue:BasicHttpValue
}
object Response {
  def apply[A](value:A) = new Response[A]{
      def basicValue = EmptyResponse.basicValue
      def value = value
    }
}
object EmptyResponse extends Response[Null] {
  def basicValue = new BasicHttpValue(
    Right(Array[Byte]()),
    200,
    "",
    Map[String,List[Header]](),
    Some(0),
    None,
    None,
    "",
    None
  )

  def value = null
}

class BasicHttpResponse[+A](val basicValue:BasicHttpValue,val value:A) extends Response[A]