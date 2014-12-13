package com.thoughtstream.betting.actor

import akka.actor.Props
import com.betfair.aping.MarketAtInPlay

/**
 *
 * @author Sateesh
 * @since 12/12/2014
 */
class FootballActor extends InformativeActor {
  private val minimumMatchedAmount = 100000
  private val betSize = 18
  val goodRunner = context.actorOf(Props(new GoodRunnerActor(1.15, 1.3, 1.7, betSize, null)), "GoodRunnerActor")
  val veryHotRunner = context.actorOf(Props(new VeryHotRunnerActor(1.2, 1.3, betSize, goodRunner)), "VeryHotRunnerActor")

  override def receive: Receive = {
    case marketAtInPlay: MarketAtInPlay =>
      marketAtInPlay.getMarketBook.setTotalMatched(150000.00)
      if (marketAtInPlay.getMarketBook.getTotalMatched < minimumMatchedAmount) {
        //ignore and drop the message....
        log info "dropping the market[%s] as total matched[%s] did not meet the configured amount[%d].".
          format(marketAtInPlay.getMarketBook.getMarketId, marketAtInPlay.getMarketBook.getTotalMatched, minimumMatchedAmount)
      } else {
        log info "Football Actor received market [%s], forwarding to VeryHotRunnerActor".format(marketAtInPlay.getMarketId)
        veryHotRunner ! marketAtInPlay
      }
  }
}
