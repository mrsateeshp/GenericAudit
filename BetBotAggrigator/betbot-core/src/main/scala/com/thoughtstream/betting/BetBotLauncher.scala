package com.thoughtstream.betting

import akka.actor._
import com.thoughtstream.betting.actor.InPlayMarketMasterActor


/**
 *
 * @author Sateesh
 * @since 12/12/2014
 */
object BetBotLauncher extends App {
  val queryMessage = "FEED_NEW_IN_PLAY_MARKETS"

  val system = ActorSystem("BetBot")
  val inPlayMarketMaster = system.actorOf(Props[InPlayMarketMasterActor], name = "InPlayMarketMasterActor")
  val betfairQueryActor = system.actorOf(Props(new BetfairQueryActor(inPlayMarketMaster, queryMessage)),"BetfairQueryActor")

  import system.dispatcher
  import scala.concurrent.duration._
  system.scheduler.schedule(1 seconds, 1 minutes) {
    betfairQueryActor ! queryMessage
  }

  system.scheduler.schedule(10 minutes, 6 hours) {
    BetfairOperations.keepConnectionLive()
  }

  def stopAllActors(): Unit ={
    system.actorSelection("/user/*") ! PoisonPill
  }
}

class BetfairQueryActor(inPlayMarketMaster: ActorRef, queryMessage: String) extends Actor{
  private val betfairOps = BetfairOperations()

  override def receive: Receive = {
    case `queryMessage` =>
      import scala.collection.JavaConversions.collectionAsScalaIterable
      betfairOps.getNewInPlayMarkets.foreach(inPlayMarketMaster ! _)
  }
}
