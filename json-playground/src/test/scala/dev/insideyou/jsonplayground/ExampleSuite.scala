package dev.insideyou
package jsonplayground

// The annotation is not required since an arbitrary instance is already derived inside of
// the Arbitraries trait which is mixed into TestSuite. It is left here only for demonstration purposes.

// @derive(arbitrary)
final case class Person(name: String, age: Int)

@SuppressWarnings(Array("org.wartremover.warts.All"))
final class ExampleSuite extends TestSuite {
  test("hello world") {
    forAll { (person: Person) =>
      expectEquals(person, person)
      expectEquals(person.name, person.name)
      expectEquals(person.age, person.age)
    }
  }
}
