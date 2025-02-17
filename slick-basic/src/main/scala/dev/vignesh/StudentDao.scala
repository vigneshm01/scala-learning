package dev.vignesh

import slick.jdbc.MySQLProfile.api._

case class Student(name: String, mark: Int)

object StudentDao {

  class StudentTable(tag: Tag) extends Table[Student](tag, Some("records"), "student"){
    def name = column[String]("name")
    def mark = column[Int]("mark")

    override def * = (name, mark) <> (Student.tupled, Student.unapply)
  }

  lazy val studentTable = TableQuery[StudentTable]
}
