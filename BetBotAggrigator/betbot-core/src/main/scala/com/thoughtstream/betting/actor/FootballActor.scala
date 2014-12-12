package com.thoughtstream.betting.actor

import com.betfair.aping.MarketAtInPlay

/**
 *
 * @author Sateesh
 * @since 12/12/2014
 */
class FootballActor extends InformativeActor {
  override def receive: Receive = {
    case marketAtInPlay: MarketAtInPlay => println("Market received: " + marketAtInPlay)
  }
}
