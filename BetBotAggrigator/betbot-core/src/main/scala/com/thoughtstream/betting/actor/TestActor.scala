package com.thoughtstream.betting.actor

import akka.actor._
import akka.routing.RoundRobinPool

/**
 * @author pinnamas
 * @since 09/12/2014
 */

class TesterMaster extends Actor with ActorLogging{

  val childActor = context.actorOf(Props[Tester].withRouter(new RoundRobinPool(5)),"childActor")

  def receive = {
    case message: String       => childActor ! message
  }
}

class Tester extends Actor with ActorLogging{
  override def preStart(): Unit = {

    log debug  "Some debug statement"
    log info "Some INFO statement"

    println("Actor Started!!!" + self.path)
  }


  override def postStop(): Unit = println("Actor Stopped!! " + self.path)

  def receive = {
    case message: String       => println(message)
  }
}

object ActorsTest extends App{
  val system = ActorSystem("TestSystem")
  val tester = system.actorOf(Props[TesterMaster], name = "tester")
  tester ! "before shutdown"
  tester ! PoisonPill
  system.actorSelection("/user/*") ! PoisonPill
  /*tester ! "after shutdown"
  tester ! "after shutdown1"
  tester ! "after shutdown1"
  tester ! "after shutdown1"
  tester ! "after shutdown1"
  tester ! "after shutdown1"*/
  Thread.sleep(3000l)
  system.shutdown()


}

