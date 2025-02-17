package dev.vignesh

import slick.jdbc.MySQLProfile.api._

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


object PrivateExecutionContext {
  val executor = Executors.newFixedThreadPool(2)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(executor)
}

object Connection{
  val db = Database.forConfig("mydb")
}

object Main{

  import PrivateExecutionContext._

  val tom = Student("tom", 90)
  val vignesh = Student("walter", 100)

  def demoInsert(): Unit = {
    val query = StudentDao.studentTable += vignesh
    val result: Future[Int] = Connection.db.run(query)

    result.onComplete{
      case Success(value) => println(s"Added $value row")
      case Failure(exp) => println(s"Exception thrown $exp")
    }

    Thread.sleep(5000)

  }

  def demoRead(): Unit = {
    val query = StudentDao.studentTable.filter(_.name.like( "vignesh"))
    val result: Future[Seq[Student]] = Connection.db.run(query.result)

    result.onComplete{
      case Success(value) => println(s"Returned $value")
      case Failure(ex) => println(s"Exception thrown $ex")
    }

    Thread.sleep(5000)

  }

  def demoUpdate(): Unit = {
    val query = StudentDao.studentTable.filter(_.name === "vignesh").update(vignesh.copy(mark = 95))
    val result: Future[Int] = Connection.db.run(query)

    result.onComplete {
      case Success(value) => println(s"Returned $value")
      case Failure(exp) => println(s"Exception thrown $exp")
    }

  }

  def main(args: Array[String]):Unit = {
    demoInsert()
  }

}