package com.thoughtstream.betting.actor

import akka.actor.ActorRef
import com.betfair.aping.MarketAtInPlay
import com.thoughtstream.betting.BetfairOperations

/**
 *
 * @author Sateesh
 * @since 12/12/2014
 */
class GoodRunnerActor(minOdds: Double, maxOdds: Double, goodRunnerThreshold: Double, betSize: Long, next: ActorRef) extends InformativeActor with SelfQueuableActor {
  private val betfairOps = BetfairOperations()

  override def receive: Receive = {
    case marketAtInPlay: MarketAtInPlay => processTheInPlayMarket(marketAtInPlay)
  }

  def processTheInPlayMarket(mInPlay: MarketAtInPlay): Unit = {
    if (mInPlay.getFavouriteRunner.getLastPriceTraded > goodRunnerThreshold) {
      passOnToNextActor(mInPlay)
    } else {
      val marketBookOptional = getMarketBookIfNotClosed(mInPlay.getMarketBook.getMarketId)
      if(marketBookOptional.isEmpty){
        //drop the market...
        log info "ignoring the market[%s] as it is closed.".format(mInPlay.getMarketBook.getMarketId)
      }else {
        processTheActiveInPlayMarket(mInPlay)
      }
    }
  }

  def passOnToNextActor(mInPlay: MarketAtInPlay) {
    if (next != null) {
      log info "passing the market[%s] to next actor as current odds[%s] higher than the configured goodRunnerThreshold[%f]".
        format(mInPlay.getMarketBook.getMarketId, mInPlay.getFavouriteRunner.getLastPriceTraded, goodRunnerThreshold)
      next ! mInPlay
    } else {
      //ignore and drop the message....
      log info "ignoring the market[%s] as current odds[%s] higher than the configured goodRunnerThreshold[%f] and next actor not configured.".
        format(mInPlay.getMarketBook.getMarketId, mInPlay.getFavouriteRunner.getLastPriceTraded, goodRunnerThreshold)
    }
  }

  def processTheActiveInPlayMarket(mInPlay: MarketAtInPlay) {
    val newMarketBook = betfairOps.getMarketBook(mInPlay.getMarketBook.getMarketId)

    val newFavRunner = findRunner(newMarketBook, mInPlay.getFavouriteRunner.getSelectionId)

    if (newFavRunner.getLastPriceTraded < minOdds || newFavRunner.getLastPriceTraded > maxOdds) {

      log info "queuing the market[%s] to itself as current odds[%s] do not match the configured. min[%f], max[%f]".
        format(mInPlay.getMarketBook.getMarketId, mInPlay.getFavouriteRunner.getLastPriceTraded, minOdds, maxOdds)

      queueMyself(mInPlay, 10)

    } else {
      log info "placing the bet for the market[%s] as current odds[%s] match the configured. min[%f], max[%f]".
        format(mInPlay.getMarketBook.getMarketId, mInPlay.getFavouriteRunner.getLastPriceTraded, minOdds, maxOdds)
      val result = placeBackBet(newFavRunner, mInPlay.getMarketCatalogue, minOdds, maxOdds, betSize, hardLine = false)
      if (!result) {
        // when not successful, message is queued for another try...
        queueMyself(mInPlay, 10)
      }
    }
  }
}
