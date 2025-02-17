
sealed trait Json

final case class JsOject(get: Map[String, Json]) extends Json
final case class JsString(get: String) extends Json
final case class JsNumber(get: Double) extends Json
final case object Null extends Json

trait JsonWriter[A] {
  def writer(value: A): Json
}

final case class Person(name:String, email: String)

object JsonWriterInstance {
  implicit val stringWriter: JsonWriter[String] = {
    new JsonWriter[String] {
      override def writer(value: String): Json = JsString(value)
    }
  }

  implicit val personWriter: JsonWriter[Person] = {
    new JsonWriter[Person] {
      override def writer(value: Person): Json =
        JsOject(Map(
          "name" -> JsString(value.name),
          "email" -> JsString(value.email)
        ))
    }
  }
}

object Json {
  def toJson[A](value: A)(implicit w: JsonWriter[A]): Json =
    w.writer(value)
}

import JsonWriterInstance._

Json.toJson(Person("Vignesh","vignesh@me.com"))






