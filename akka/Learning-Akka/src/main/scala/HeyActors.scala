
import akka.actor._

class HelloActor extends Actor{
  def receive = {
    case "Hi" => sender() ! "Hello"
    case "How are you?" => sender() ! "what do you want?"
  }
}


class HeyActor extends Actor{

  def receive = {
    case "Hello" => println("seems like a nice person")
    case "what do you want?" => println("ah bro why did you say that!")
    case _ => println("yess..")
  }

}

object HeyActors extends App {
  val system = ActorSystem("New")
  val person: ActorRef = system.actorOf(Props[HelloActor](), "person")
  val sender = system.actorOf(Props[HeyActor](), "sender")

  person.tell("Hi", sender)
  person.tell("How are you?", sender)

  Thread.sleep(1000)
  system.terminate()

}

