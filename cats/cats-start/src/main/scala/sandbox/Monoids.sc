trait Semigroup[A] {
  def combine(x: A, y: A): A
}
trait Monoid[A] extends Semigroup[A] {
  def empty: A
}

val y = Some(2)

val z = None

y.map(_*3)

y.flatMap(_*3)