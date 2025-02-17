import async.Async.insist

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val f = Future { "hi" }
val g = Future { throw new RuntimeException("error")}
def insist[A](makeAsyncComputation: () => Future[A], maxAttempts: Int): Future[A] = {
  println("here i am")
  if (maxAttempts > 1)
    makeAsyncComputation() fallbackTo insist(makeAsyncComputation, maxAttempts-1)
  else makeAsyncComputation()
}

val h = insist(() => f ,4)
h foreach println // Eventually prints 5