
def processList[T](x: List[T]): T = {
  x match{
    case x: List[Int] => x.sum
    case x: List[String] => x.mkString("")
    case _ => throw UnsupportedOperationException
  }
}