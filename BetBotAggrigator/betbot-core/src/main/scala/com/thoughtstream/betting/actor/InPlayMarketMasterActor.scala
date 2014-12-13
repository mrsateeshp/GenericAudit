package com.thoughtstream.betting.actor

import akka.actor.Props
import com.betfair.aping.MarketAtInPlay

/**
 *
 * @author Sateesh
 * @since 11/12/2014
 */
class InPlayMarketMasterActor extends InformativeActor {

  val footballActor = context.actorOf(Props[FootballActor], "American_Football")

  def dispatchToActor(marketAtInPlay: MarketAtInPlay): Unit = {
    val sportType: String = marketAtInPlay.getMarketCatalogue.getEventType.getName
    val actorOptional = context.child(sportType.replaceAll(" ","_"))
    if (actorOptional.isDefined) {
      actorOptional.get ! marketAtInPlay
    } else {
      log info "Sport[%s] is ignored as it is not yet configured in InPlayMarketMasterActor.".format(sportType)
    }
  }

  override def receive: Receive = {
    case marketAtInPlay: MarketAtInPlay => dispatchToActor(marketAtInPlay)
  }
}

