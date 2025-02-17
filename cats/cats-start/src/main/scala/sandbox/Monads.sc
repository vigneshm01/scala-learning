import cats.syntax.either._ // for asRight

val a = 3.asRight[String]
// a: Either[String, Int] = Right(3)
val b = Right(4)
// b: Either[String, Int] = Right(4)
for {
  x <- a
  y <- b
} yield x*x + y*y

def countPositive(nums: List[Int]) =
  nums.foldLeft(0.asRight[String]) { (accumulator, num) =>
    if(num > 0) {
      accumulator.map(_ + 1)
    } else {
      Left("Negative. Stopping!")
    }
  }

countPositive(List(1, 2, 3))

import cats.data.Reader
final case class Cat(name: String, favoriteFood: String)
val catName: Reader[Cat, String] =
  Reader(cat => cat.name)
// catName: Reader[Cat, String] = Kleisli(<function1>)

val greetKitty: Reader[Cat, String] =
  catName.map(name => s"Hello ${name}")
greetKitty.run(Cat("Heathcliff", "junk food"))
// res2: cats.package.Id[String] = "Hello Heathcliff"
