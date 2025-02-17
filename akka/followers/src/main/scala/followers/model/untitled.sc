import slick.jdbc.H2Profile.api._
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy, Supervision}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object FilteredSlickAkkaStreamExample extends App {
  implicit val system: ActorSystem = ActorSystem("FilteredSlickAkkaStreamSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  // Source database configuration
  val sourceDbConfig = Database.forConfig("sourceDb")

  // Target database configuration
  val targetDbConfig = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl("jdbc:h2:mem:targetDb")
    hikariConfig.setUsername("sa")
    hikariConfig.setPassword("")
    hikariConfig.setMaximumPoolSize(20)
    Database.forDataSource(new HikariDataSource(hikariConfig), None)
  }

  // Filter database configuration
  val filterDbConfig = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl("jdbc:h2:mem:filterDb")
    hikariConfig.setUsername("sa")
    hikariConfig.setPassword("")
    hikariConfig.setMaximumPoolSize(10)
    Database.forDataSource(new HikariDataSource(hikariConfig), None)
  }

  // Table definitions
  class SourceTable(tag: Tag) extends Table[(Int, String)](tag, "source_table") {
    def id = column[Int]("id", O.PrimaryKey)
    def name = column[String]("name")
    def * = (id, name)
  }
  val sourceTable = TableQuery[SourceTable]

  class TargetTable(tag: Tag) extends Table[(Int, String)](tag, "target_table") {
    def id = column[Int]("id", O.PrimaryKey)
    def name = column[String]("name")
    def * = (id, name)
  }
  val targetTable = TableQuery[TargetTable]

  class FilterTable(tag: Tag) extends Table[(Int, Boolean)](tag, "filter_table") {
    def id = column[Int]("id", O.PrimaryKey)
    def isValid = column[Boolean]("is_valid")
    def * = (id, isValid)
  }
  val filterTable = TableQuery[FilterTable]

  // Streaming query
  val chunkSize = 10000
  val streamingQuery = sourceTable.result
    .transactionally
    .withStatementParameters(fetchSize = chunkSize)

  // Filter function
  def filterRecord(record: (Int, String)): Future[Boolean] = {
    filterDbConfig.run(filterTable.filter(_.id === record._1).result.headOption).map {
      case Some((_, isValid)) => isValid
      case None => false // If not found in filter table, exclude it
    }
  }

  // Modified flow with filtering
  val batchSize = 1000
  val maxConcurrentBatches = 10
  val processFlow = Flow[(Int, String)]
    .mapAsync(maxConcurrentBatches) { record =>
      filterRecord(record).map(isValid => (record, isValid))
    }
    .filter(_._2) // Keep only records where isValid is true
    .map(_._1) // Extract the original record
    .grouped(batchSize)
    .mapAsync(maxConcurrentBatches) { batch =>
      targetDbConfig.run(DBIO.seq(
        targetTable ++= batch
      ).transactionally)
    }
    .buffer(16, OverflowStrategy.backpressure)

  // Error handling
  case class BatchInsertException(message: String, recordsInserted: Long) extends Exception(message)

  // Set up and run the stream
  val streamingSource = Source.fromPublisher(sourceDbConfig.stream(streamingQuery))
  var recordsInserted: Long = 0
  var recordsProcessed: Long = 0

  val done = streamingSource
    .via(processFlow)
    .map { _ =>
      recordsInserted += batchSize
      recordsProcessed += batchSize
      (recordsProcessed, recordsInserted)
    }
    .watchTermination() { (_, termination) =>
      termination.flatMap {
        case Success(_) => Future.successful((recordsProcessed, recordsInserted))
        case Failure(ex) => Future.failed(BatchInsertException(s"Insert failed after processing $recordsProcessed and inserting $recordsInserted records", recordsInserted))
      }
    }
    .runWith(Sink.ignore)

  // Handle completion or failure
  done.onComplete {
    case Success((processed, inserted)) =>
      println(s"Stream completed successfully. Records processed: $processed, Records inserted: $inserted")
    case Failure(BatchInsertException(msg, count)) =>
      println(s"$msg. You may need to clean up or retry the remaining records.")
    case Failure(ex) => println(s"Stream failed: ${ex.getMessage}")
  }

  // Graceful shutdown
  done.flatMap { _ =>
    system.terminate()
  }.flatMap { _ =>
    sourceDbConfig.shutdown
  }.flatMap { _ =>
    targetDbConfig.shutdown
  }.flatMap { _ =>
    filterDbConfig.shutdown
  }.onComplete {
    case Success(_) => println("System shutdown complete")
    case Failure(e) => println(s"Error during shutdown: ${e.getMessage}")
  }
}