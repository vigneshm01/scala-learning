import cats.data.State
import cats.implicits._

// Define your State type
case class AppState(
                     holdings: Map[String, Double],
                     transactions: List[Transaction],
                     // other state fields...
                   )

// Define your RefDataState type alias
type RefDataState[A] = State[AppState, A]

// ContainerService
class ContainerService {
  // Example operation: add a new holding
  def addHolding(id: String, amount: Double): RefDataState[Unit] =
    State.modify { state =>
      state.copy(holdings = state.holdings + (id -> amount))
    }

  // Example operation: get a holding
  def getHolding(id: String): RefDataState[Option[Double]] =
    State.inspect { state =>
      state.holdings.get(id)
    }

  // Example operation: add a transaction
  def addTransaction(transaction: Transaction): RefDataState[Unit] =
    State.modify { state =>
      state.copy(transactions = transaction :: state.transactions)
    }

  // Compose multiple operations
  def processNewHolding(id: String, amount: Double): RefDataState[Option[Double]] =
    for {
      _ <- addHolding(id, amount)
      _ <- addTransaction(Transaction(id, amount, "NEW"))
      holding <- getHolding(id)
    } yield holding

  // Example of how to run the state monad
  def runOperation[A](operation: RefDataState[A], initialState: AppState): (AppState, A) =
    operation.run(initialState).value
}

// Example usage
object ContainerServiceExample extends App {
  val service = new ContainerService
  val initialState = AppState(Map.empty, List.empty)

  val operation = service.processNewHolding("AAPL", 100.0)
  val (newState, result) = service.runOperation(operation, initialState)

  println(s"Result: $result")
  println(s"New state: $newState")
}

case class Transaction(id: String, amount: Double, tType: String)