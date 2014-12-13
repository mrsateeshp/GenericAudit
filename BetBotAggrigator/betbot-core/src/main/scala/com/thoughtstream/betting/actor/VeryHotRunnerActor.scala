package com.thoughtstream.betting.actor

import akka.actor.ActorRef
import com.betfair.aping.MarketAtInPlay
import com.thoughtstream.betting.BetfairOperations

/**
 *
 * @author Sateesh
 * @since 12/12/2014
 */
class VeryHotRunnerActor(minOdds: Double, maxOdds: Double, betSize: Long, next: ActorRef) extends InformativeActor {
  private val betfairOps = BetfairOperations()
  private val maxAccetableOdds = 1.8

  override def receive: Receive = {
    case marketAtInPlay: MarketAtInPlay => processTheMessage(marketAtInPlay)
  }

  def processTheMessage(mInPlay: MarketAtInPlay): Unit = {
    if (mInPlay.getFavouriteRunner.getLastPriceTraded < minOdds) {
      val newMarketBook = betfairOps.getMarketBook(mInPlay.getMarketBook.getMarketId)
      val newFavRunner = findRunner(newMarketBook, mInPlay.getFavouriteRunner.getSelectionId)
      if (newFavRunner.getLastPriceTraded < minOdds) {
        import scala.concurrent.duration._
        context.system.scheduler.scheduleOnce(10 seconds) {
          self ! mInPlay
        }(context.system.dispatcher)
      } else {
        log info "placing the best for the market[%s] as current odds[%s] match the configured. min[%f], max[%f]".
          format(mInPlay.getMarketBook.getMarketId, mInPlay.getFavouriteRunner.getLastPriceTraded, minOdds, maxOdds)
        //TODO:place the bet with out any target price...
        placeBackBet(newFavRunner, mInPlay.getMarketCatalogue, minOdds, maxAccetableOdds, betSize, hardLine = false)
      }
    } else if (mInPlay.getFavouriteRunner.getLastPriceTraded <= maxOdds) {
      log info "placing the best for the market[%s] as current odds[%s] match the configured. min[%f], max[%f]".
        format(mInPlay.getMarketBook.getMarketId, mInPlay.getFavouriteRunner.getLastPriceTraded, minOdds, maxOdds)
      //TODO:place the bet with out any target price...
      placeBackBet(mInPlay.getFavouriteRunner, mInPlay.getMarketCatalogue, minOdds, maxAccetableOdds, betSize, hardLine = false)
    } else {
      //passing it to next actor in the chain
      log info "passing the market[%s] to next actor as current odds[%s] dont match the configured. min[%f], max[%f]".
        format(mInPlay.getMarketBook.getMarketId, mInPlay.getFavouriteRunner.getLastPriceTraded, minOdds, maxOdds)

      if (next != null) {
        next ! mInPlay
      }
    }
  }
}
