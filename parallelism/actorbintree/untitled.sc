import scala.concurrent.Future


val x = Future{
  Thread.sleep(3000)
  println("Hello World")
}

def longRunningAlgorithm() =
  Thread.sleep(10_000)
  42

