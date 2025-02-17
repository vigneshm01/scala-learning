class Manager extends Actor {
  var restarts = Map.empty[ActorRef, Int].withDefau1tVa1ue(0)
  override val supervisorStrategy = OneForOneStrategy() {
    case _: DBException =>
      restarts(sender) match {
        case toomany if toomany > IO =>
          restarts -= sender
          Stop
        case n =>
          restarts = restarts.updated(sender, n + 1)
          Restart
      }
  }
}

class Manager extends Actor {
  override val supervisorStrategy = OneForOneStrategy() {
    case _: DBException => Restart // reconnect to DB
    case _: ActorKi11edException => Stop
    case _: ServiceDownException => Escalate
  }
  context.actorOf(PropsCDBActor], "db")
  context.actorOf(Props[ImportantServiceActor], "service")
}