package com.thoughtstream.betting.actor

import akka.actor.ActorRef
import com.betfair.aping.MarketAtInPlay
import com.thoughtstream.betting.BetfairOperations

/**
 *
 * @author Sateesh
 * @since 12/12/2014
 */
class GoodRunnerActor(minOdds: Double, maxOdds: Double, goodRunnerThreshold: Double, betSize: Long, next: ActorRef) extends InformativeActor {
  private val betfairOps = BetfairOperations()

  override def receive: Receive = {
    case marketAtInPlay: MarketAtInPlay => processTheMessage(marketAtInPlay)
  }

  def processTheMessage(mInPlay: MarketAtInPlay): Unit = {
    if (mInPlay.getFavouriteRunner.getLastPriceTraded > goodRunnerThreshold) {
      //ignore and drop the message....
      log info "passing the market[%s] to next actor as current odds[%s] bigger than the configured goodRunnerThreshold[%f]".
        format(mInPlay.getMarketBook.getMarketId, mInPlay.getFavouriteRunner.getLastPriceTraded, goodRunnerThreshold)
      if (next != null) {
        next ! mInPlay
      }
    } else {
      val newMarketBook = betfairOps.getMarketBook(mInPlay.getMarketBook.getMarketId)
      val newFavRunner = findRunner(newMarketBook, mInPlay.getFavouriteRunner.getSelectionId)

      if (newFavRunner.getLastPriceTraded < minOdds || newFavRunner.getLastPriceTraded > maxOdds) {

        log info "queuing the market[%s] to itself as current odds[%s] do not match the configured. min[%f], max[%f]".
          format(mInPlay.getMarketBook.getMarketId, mInPlay.getFavouriteRunner.getLastPriceTraded, minOdds, maxOdds)

        import scala.concurrent.duration._
        context.system.scheduler.scheduleOnce(10 seconds) {
          self ! mInPlay
        }(context.system.dispatcher)

      } else {
        log info "placing the best for the market[%s] as current odds[%s] match the configured. min[%f], max[%f]".
          format(mInPlay.getMarketBook.getMarketId, mInPlay.getFavouriteRunner.getLastPriceTraded, minOdds, maxOdds)
        //TODO:place the bet with out any target price...
        val result = placeBackBet(newFavRunner, mInPlay.getMarketCatalogue, minOdds, maxOdds, betSize, hardLine = false)
      }
    }
  }

}
