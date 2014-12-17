package com.thoughtstream.betting

import java.math.{BigDecimal, RoundingMode}
import java.util

import com.betfair.aping.api.BetfairOperations
import com.betfair.aping.entities._
import com.betfair.aping.enums._
import com.betfair.aping.exceptions.APINGException
import org.slf4j.LoggerFactory

/**
 *
 * @author Sateesh
 * @since 13/12/2014
 */
package object actor {
  val log = LoggerFactory.getLogger(classOf[BetfairOperations])
  private val betfairOps = BetfairOperations()

  def findRunner(marketBook: MarketBook, selectionId: Long): Runner = {
    import scala.collection.JavaConversions._
    for (runner <- marketBook.getRunners) {
      if (runner.getSelectionId == selectionId) {
        return runner
      }
    }
    throw new RuntimeException("Illigal state!!!")
  }

  def round(value: Double): Double = {
    val places: Int = 2
    if (places < 0) throw new IllegalArgumentException
    var bd: BigDecimal = new BigDecimal(value)
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    bd.doubleValue
  }

  var customerRef: Long = 1l

  def getCustomerRef: String = {
    customerRef = customerRef + 1
    customerRef.toString
  }

  @throws(classOf[APINGException])
  def getMarketBookIfNotClosed(marketId: String): Option[MarketBook] = {
    val marketBook = betfairOps.getMarketBook(marketId)

    if(marketBook.getStatus.equalsIgnoreCase("CLOSED")) {
      None
    } else {
      Some(marketBook)
    }
  }

  def placeBackBet(runner: Runner, marketCatalogue: MarketCatalogue, minOdds: Double, maxOdds: Double, betSize: Long, hardLine: Boolean = false): Boolean = {
    val marketIdChosen: String = marketCatalogue.getMarketId
    val selectionId: Long = runner.getSelectionId

    log.info("Place a bet below minimum stake to prevent the bet actually " + "being placed for marketId: " + marketIdChosen + " with selectionId: " + selectionId + "...\n\n")
    val instructions: java.util.List[PlaceInstruction] = new util.ArrayList[PlaceInstruction]
    val instruction: PlaceInstruction = new PlaceInstruction
    instruction.setHandicap(0)
    instruction.setSide(Side.BACK)
    instruction.setOrderType(OrderType.LIMIT)
    val limitOrder: LimitOrder = new LimitOrder
    limitOrder.setPersistenceType(PersistenceType.PERSIST)
    var price: Double = runner.getLastPriceTraded - .05

    if (runner.getLastPriceTraded < minOdds || runner.getLastPriceTraded > maxOdds) {
      log.info("Skipping to place the bet for market " + marketCatalogue.getMarketName + " as odds changed... new odds: " + runner.getLastPriceTraded)
      return false
    }
    if (price < 1.01) {
      price = 1.01
    }
    price = round(price)
    log.info("Placing the bet for market " + marketCatalogue.getMarketName + " at the odds of: " + price)
    limitOrder.setPrice(price)
    limitOrder.setSize(betSize)
    instruction.setLimitOrder(limitOrder)
    instruction.setSelectionId(selectionId)
    instructions.add(instruction)
    if (!hardLine) {
      try {
        Thread.sleep(15000l)
      }
      catch {
        case e: InterruptedException => {
          e.printStackTrace
        }
      }
      return placeBackBet(runner, marketCatalogue, minOdds, maxOdds, betSize, hardLine = true)

    }
    val customerRef: String = getCustomerRef
    val placeBetResult: PlaceExecutionReport = betfairOps.placeOrders(marketIdChosen, instructions, customerRef)
    if (placeBetResult.getStatus == ExecutionReportStatus.SUCCESS) {
      log.info("Your bet has been placed!!")
      log.info(placeBetResult.getInstructionReports.toString)
      true
    } else {
      log.error("NORMAL: Your bet has NOT been placed :*( amount: " + betSize + " odds: " + price)
      log.error("NORMAL: The error is: " + placeBetResult.getErrorCode + ": " + placeBetResult.getErrorCode.getMessage)
      false
    }
  }
}
