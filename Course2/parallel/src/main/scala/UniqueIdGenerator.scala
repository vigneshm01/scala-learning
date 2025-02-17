object UniqueIdGenerator {
  private var uidCount = 0L

  def getUniqueId(): Long = this.synchronized {
    uidCount = uidCount + 1
    uidCount
  }

  def startThread(): Thread = {
    val t = new Thread {
      override def run(): Unit = {
        val ids = for (i <- 1 to 10) yield getUniqueId()
        println(ids)
      }
    }
    t.start()
    t
  }

  def main(args: Array[String]): Unit = {
    startThread()
    startThread()
  }
}

