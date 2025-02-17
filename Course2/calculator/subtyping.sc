
abstract class CaffeinatedBeverage{
  def caffeineContent:Int
}

final case class FilterCoffee(override val caffeineContent: Int, region: String) extends CaffeinatedBeverage

final case class CuteMate(override val caffeineContent: Int) extends CaffeinatedBeverage

object CaffeinatedBeverage:
  def chose[A <: CaffeinatedBeverage](x:A, y:A): A =
    if x.caffeineContent >= y.caffeineContent then x
    else y

abstract class CaffeineSource[+A <: CaffeinatedBeverage] {
  def pull(): A
}

class CuteMateSource extends CaffeineSource[CuteMate] {
  override def pull(): CuteMate = CuteMate(85)
}

class FilterCoffeeSource extends CaffeineSource[FilterCoffee] {
  override def pull(): FilterCoffee = FilterCoffee(69, "Ethiopia")
}

object AgileFragile{
  val caffeineSource: CaffeineSource[CaffeinatedBeverage] = new FilterCoffeeSource //covariant example
}

final case class Deliverable(description: String)

class Programmer[-A <: CaffeinatedBeverage]:
  def transform(caffeine: A, feature: String): Deliverable = Deliverable(feature)
  
object Startupr:
  def deliver(
               feature: String,
               programmer: Programmer[CuteMate], // programmer can accept any caffeinated beverage but 
               caffeineSource: CaffeineSource[CuteMate]
             ):Deliverable = programmer.transform(caffeineSource.pull(), feature)
  
  val cto = new Programmer[CaffeinatedBeverage] //contravariant example
  val caffeineSource: CaffeineSource[CuteMate] = new CuteMateSource
  
  def main(arg: Array[String]): Unit =
    deliver("Emojis", cto, caffeineSource)
    
    
    
    