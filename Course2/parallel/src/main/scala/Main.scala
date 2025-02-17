
object Main extends App{

//    class Account(private var amount: Int = 0) {
//        def transfer(target: Account, n: Int) =
//            this.synchronized {
//                println("sender is synchronized")
//                target.synchronized {
//                    println("receiver is synchronized")
//                    this.amount -= n
//                    target.amount += n
//                }
//            }
//    }
//
//    def startThead(a: Account, b: Account, amount: Int) = {
//        val t = new Thread {
//            override def run() =
//                for (i <- 1 to amount) {
//                    a.transfer(b, 1)
//                }
//        }
//        t.start()
//        t
//    }
//
//    val a1 = new Account(1000)
//    val a2 = new Account(5000)
//
//    println("starting first")
//    val t = startThead(a1, a2, 500)
//    println("starting second")
//    val s = startThead(a2, a1, 1000)
//
//    t.join()
//    s.join()

    private val x = new AnyRef{}
    private var uidCount = 0L

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

    strartTheard()
    strartTheard()

}

