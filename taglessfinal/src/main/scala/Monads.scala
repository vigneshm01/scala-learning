class Monads {

  case class SafeValue[+T](private val internalValue: T){
    def get: T = synchronized{
      internalValue
    }

    def transform[S](transfromer: T => SafeValue[S]): SafeValue[S] = {
      transfromer(internalValue)
    }
  }

  def gimmeSafeValue[T](value: T): SafeValue[T] = SafeValue(value)

  val firstSafeValue: SafeValue[String] = gimmeSafeValue("You are going good Mr.Wayne")
  val string:String = firstSafeValue.get
  val upperString = string.toUpperCase()
  val saferUpperString = SafeValue(upperString)

  val transformedSaferString = firstSafeValue.transform(s => SafeValue(s.toUpperCase()))
}

def nextList(x: Int) = List(x, x + 1)
nextList(3) // List(3,4)
List(3).flatMap(nextList) //List(3,4)

// Monad(x).flatMap(f) == f(x)