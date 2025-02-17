package dev.insideyou
package jsonplayground

import io.circe._
import io.circe.parser._
import io.circe.generic.auto._

object Main {
  def main(args: Array[String]): Unit = {
    final case class Person(name: String, age: Int)
    val person = Person("Vignesh", 22)
    val personInJson: String = Encoder[Person].apply(person).toString
    println(personInJson)
    val personClass: Either[Error, Person] = decode[Person](personInJson)
    print(personClass)
    val withoutGeneric = parse(personInJson).flatMap(Decoder[Person].decodeJson)


  }
}
