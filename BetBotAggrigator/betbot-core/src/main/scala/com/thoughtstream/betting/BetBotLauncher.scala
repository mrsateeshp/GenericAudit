package com.thoughtstream.betting

import akka.actor.{Props, Actor, ActorRef, ActorSystem}
import com.thoughtstream.betting.actor.InPlayMarketMasterActor


/**
 *
 * @author Sateesh
 * @since 12/12/2014
 */
object BetBotLauncher extends App {
  private val queryMessage = "FEED_NEW_IN_PLAY_MARKETS"

  class BetfairQueryActor(inPlayMarketMaster: ActorRef) extends Actor{
    override def receive: Receive = {
      case `queryMessage` =>
        import scala.collection.JavaConversions.collectionAsScalaIterable
        val betfairOps = BetfairOperations()
        betfairOps.getNewInPlayMarkets.foreach(inPlayMarketMaster ! _)
    }
  }

  val system = ActorSystem("BetBot")
  val inPlayMarketMaster = system.actorOf(Props[InPlayMarketMasterActor], name = "InPlayMarketMasterActor")
  val betfairQueryActor = system.actorOf(Props(new BetfairQueryActor(inPlayMarketMaster)),"BetfairQueryActor")

  import system.dispatcher
  import scala.concurrent.duration._
  system.scheduler.schedule(1 seconds, 1 minutes) {
    betfairQueryActor ! queryMessage
  }
}
