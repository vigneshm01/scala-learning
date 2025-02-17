/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package actorbintree

import akka.actor._
import scala.collection.immutable.Queue

object BinaryTreeSet {

  sealed trait Operation {
    def requester: ActorRef
    def id: Int
    def elem: Int
  }

  trait OperationReply {
    def id: Int
  }

  /** Request with identifier `id` to insert an element `elem` into the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Insert(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to check whether an element `elem` is present
    * in the tree. The actor at reference `requester` should be notified when
    * this operation is completed.
    */
  case class Contains(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to remove the element `elem` from the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Remove(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request to perform garbage collection */
  case object GC

  /** Holds the answer to the Contains request with identifier `id`.
    * `result` is true if and only if the element is present in the tree.
    */
  case class ContainsResult(id: Int, result: Boolean) extends OperationReply

  /** Message to signal successful completion of an insert or remove operation. */
  case class OperationFinished(id: Int) extends OperationReply

}


class BinaryTreeSet extends Actor with Stash {
  import BinaryTreeSet._
  import BinaryTreeNode._

  def createRoot: ActorRef = context.actorOf(BinaryTreeNode.props(0, initiallyRemoved = true))

  var root = createRoot

  // optional (used to stash incoming operations during garbage collection)
  var pendingQueue = Queue.empty[Operation]

  // optional
  def receive = normal

  // optional
  /** Accepts `Operation` and `GC` messages. */
  val normal: Receive = {
    case op: Operation => root ! op
    case GC =>
      val newRoot = createRoot
      context.become(garbageCollecting(newRoot))
      root ! CopyTo(newRoot)
  }

  // optional
  /** Handles messages while garbage collection is performed.
    * `newRoot` is the root of the new binary tree where we want to copy
    * all non-removed elements into.
    */
  def garbageCollecting(newRoot: ActorRef): Receive = {
    case op: Operation => stash()
    case CopyFinished =>
      root ! PoisonPill
      root = newRoot
      context.become(normal)
      unstashAll()
  }

}

object BinaryTreeNode {
  trait Position

  case object Left extends Position
  case object Right extends Position

  case class CopyTo(treeNode: ActorRef)
  /**
   * Acknowledges that a copy has been completed. This message should be sent
   * from a node to its parent, when this node and all its children nodes have
   * finished being copied.
   */
  case object CopyFinished

  def props(elem: Int, initiallyRemoved: Boolean) = Props(classOf[BinaryTreeNode],  elem, initiallyRemoved)
}

class BinaryTreeNode(val elem: Int, initiallyRemoved: Boolean) extends Actor {
  import BinaryTreeNode._
  import BinaryTreeSet._

  var subtrees = Map[Position, ActorRef]()
  var removed = initiallyRemoved

  // optional
  def receive = normal

  // optional
  /** Handles `Operation` messages and `CopyTo` requests. */
  val normal: Receive = {
    case Contains(sender, id, value) => contains(sender, id, value)
    case Insert(sender, id, value) => insertElem(sender, id, value)
    case Remove(sender, id, value) => remove(sender, id, value)
    case CopyTo(newRoot) =>
      if (!removed) newRoot ! Insert(self, elem, elem)
      subtrees.values.foreach(_ ! CopyTo(newRoot))
      context.become(copying(subtrees.values.toSet, removed))
  }

  def contains(sender: ActorRef, id: Int, value: Int): Unit = {
    if (elem == value && !removed) sender ! ContainsResult(id, true)
    else if (subtrees.nonEmpty)
      if (value > elem) {
        if (subtrees.contains(Right)) subtrees(Right) ! Contains(sender, id, value)
        else sender ! ContainsResult(id, false)
      }
      else {
        if (subtrees.contains(Left)) subtrees(Left) ! Contains(sender, id, value)
        else sender ! ContainsResult(id, false)
      }
    else sender ! ContainsResult(id, false)
  }

  def insertElem(sender:ActorRef, id:Int, value:Int): Unit = {
    if (elem == value) {
      sender ! OperationFinished(id)
      removed = false
    } else if (value > elem) {
      if (subtrees.contains(Right)) subtrees(Right) ! Insert(sender, id, value)
      else {
        val rightActor: ActorRef = context.actorOf(BinaryTreeNode.props(value, initiallyRemoved = false))
        subtrees = subtrees + (Right -> rightActor)
        sender ! OperationFinished(id)
      }
    }
    else {
      if (subtrees.contains(Left)) subtrees(Left) ! Insert(sender, id, value)
      else {
        val leftActor: ActorRef = context.actorOf(BinaryTreeNode.props(value, initiallyRemoved = false))
        subtrees = subtrees + (Left -> leftActor)
        sender ! OperationFinished(id)
      }
    }

  }

  def remove(sender:ActorRef, id:Int, value:Int): Unit =  {
    if (elem == value) {
      removed = true
      sender ! OperationFinished(id)
    }
    else if (subtrees.nonEmpty)
      if (value > elem) {
        if (subtrees.contains(Right)) subtrees(Right) ! Remove(sender, id, value)
        else sender ! OperationFinished(id)
      }
      else {
        if (subtrees.contains(Left)) subtrees(Left) ! Remove(sender, id, value)
        else sender ! OperationFinished(id)
      }
    else sender ! OperationFinished(id)
  }

  // optional
  /** `expected` is the set of ActorRefs whose replies we are waiting for,
    * `insertConfirmed` tracks whether the copy of this node to the new tree has been confirmed.
    */
  def copying(expected: Set[ActorRef], insertConfirmed: Boolean): Receive = {
    if (insertConfirmed && expected.isEmpty) {
      context.parent ! CopyFinished
      context.become(normal) // Switch back to normal for future
      normal // Return normal for now
    } else {
      case OperationFinished(id) => if (id == elem) context.become(copying(expected, insertConfirmed = true))
      case CopyFinished          => context.become(copying(expected - sender, insertConfirmed))
    }
  }


}
