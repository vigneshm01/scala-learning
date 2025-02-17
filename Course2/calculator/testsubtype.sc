abstract class Person {
  def name: String
  def age: Int
  // address and other properties
  // methods (ideally only accessors since it is a case class)
}

case class Employer(val name: String, val age: Int)
  extends Person

case class Employee(val name: String, val age: Int)
  extends Person
  
val employer = Employer("Bofa", 200)
val employee = Employee("Vignesh",22)

val x: List[Person] = List(employee, employer)

def takeit(x:Person):String = x.name

x.map(takeit(_))

