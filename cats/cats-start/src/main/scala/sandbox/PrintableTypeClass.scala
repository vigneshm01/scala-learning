package sandbox

object PrintableTypeClass extends App {
  println("Hello TypeClasses")

  import PrintableInstance._

  Printable.print(2)
}

trait Printable[A] {
  def format(value: A): String
}

final case class Cat(name: String, age: Int, color: String)

object PrintableInstance {
  implicit val printableInt: Printable[Int] = new Printable[Int] {
    override def format(value: Int): String = s"Printing ${value.toString}"
  }

  implicit val printableString: Printable[String] = new Printable[String] {
    override def format(value: String): String = s"Printing $value"
  }
}

object Printable{

  def format[A](value: A)(implicit converter: Printable[A]) = {
    converter.format(value)
  }

  def print[A](value: A)(implicit converter: Printable[A]) = {
    println(converter.format(value))
  }
}