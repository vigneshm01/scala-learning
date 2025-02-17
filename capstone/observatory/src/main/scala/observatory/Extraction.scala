package observatory

import java.time.LocalDate
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import scala.io.Source

/**
  * 1st milestone: data extraction
  */
object Extraction extends ExtractionInterface:
  Logger // TODO
    .getLogger("org.apache.spark")
    .setLevel(Level.WARN)

  val conf: SparkConf = // TODO
    new SparkConf()
      .setMaster("local")
      .setAppName("Observatory")

  val sc: SparkContext = new SparkContext(conf) // TODO

  /**
   * Load the resource from filesystem as RDD[String]
   *
   * @param resource the resource path
   * @return the resource content as RDD[String]
   */
  def getRDDFromResource(resource: String): RDD[String] =
    val fileStream =
      Source
        .getClass
        .getResourceAsStream(resource)

    sc.makeRDD(
      Source
        .fromInputStream(fileStream)
        .getLines
        .toSeq)
  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] =
    val statRDD: RDD[((String, String), Location)] =
      getRDDFromResource(stationsFile)
        .map(line => line.split(",", -1))
        .filter {
          case Array(_, _, "", _) => false
          case Array(_, _, _, "") => false
          case _ => true
        }
        .map(a => (
          (a(0), a(1)), Location(a(2).toDouble, a(3).toDouble)
        ))

    val tempRDD: RDD[((String, String), (LocalDate, Temperature))] =
      getRDDFromResource(temperaturesFile)
        .map(line => line.split(",", -1))
        .map(a => (
          (a(0), a(1)),
          (LocalDate.of(year, a(2).toInt, a(3).toInt), a(4).toDouble)
        ))

    statRDD
      .join(tempRDD)
      .mapValues { case (loc, (date, temp)) =>
        (date, loc, (temp - 32.0) / 1.8)
      } // LAZY up to here!
      .values
      .collect


  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] =
    records
      .groupBy(_._2)
      .view
      .mapValues(iter => iter.map(_._3))
      .mapValues(iter => iter.sum / iter.size)

