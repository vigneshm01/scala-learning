val x = new AnyRef()
var uidCount = 0L

def getUniqueId(): Long = x.synchronized {
  uidCount = uidCount + 1
  uidCount
}

def strartTheard() =
  val t = new Thread {
    override def run(): Unit =
      val ids = for (i <- 1 to 10) yield getUniqueId()
      println(ids)
  }
  t.start()
  t

println(strartTheard())
println(strartTheard())
