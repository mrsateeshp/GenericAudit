package com.thoughtstream.betting

import com.betfair.aping.api.{BetfairJsonRpcOperations, BetfairOperations, LoginUtils}

/**
 *
 * @author Sateesh
 * @since 12/12/2014
 */
object BetfairOperations {

  import com.thoughtstream.betting.Configuration.config

  private val appKey = config.getString("betfair.appKey")
  private val uid = config.getString("betfair.uid")
  private val password = config.getString("betfair.password")
  private lazy val ssoToken = "PsAwjVjf8tMHLyguwc4SsWQHxH8wZREQAGel8LV9fjU="
//  private lazy val ssoToken = LoginUtils.getSSOToken(appKey, uid, password)
  private lazy val betfairOperations = new BetfairJsonRpcOperations(appKey, ssoToken)

  def keepConnectionLive(): Unit = LoginUtils.keepConnectionLive(appKey, ssoToken)

  def apply(): BetfairOperations = betfairOperations
}
