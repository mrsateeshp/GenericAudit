package com.thoughtstream.web.controller;

import com.betfair.aping.ApiNGJsonRpcDemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static com.betfair.aping.ApiNGJsonRpcDemo.placedMarkets;
import static com.betfair.aping.ApiNGJsonRpcDemo.marketsAtInPlay;

/**
 * This is a demonstration class to show a quick demo of the new Betfair API-NG.
 * When you execute the class will: <li>find a market (next horse race in the
 * UK)</li> <li>get prices and runners on this market</li> <li>place a bet on 1
 * runner</li> <li>handle the error</li>
 *
 */
public class ProfitOrLossBookingTread extends Thread {

    static final Logger logger = LoggerFactory.getLogger(ProfitOrLossBookingTread.class);
    private static Set<String> runningMarkets = new HashSet<String>();
    private static Set<String> bookedMarkets = new HashSet<String>();

    @Override
    public void run() {
        while(true) {
            logger.info("Running ProfitOrLossBookingTread");
            try {
                for (String marketId : placedMarkets) {
                    if (ApiNGJsonRpcDemo.isMarketClosed(marketId)){
                        placedMarkets.remove(marketId);
                        marketsAtInPlay.remove(marketId);
                        runningMarkets.remove(marketId);
                        bookedMarkets.remove(marketId);
                    }else {
                        if (!(runningMarkets.contains(marketId) || bookedMarkets.contains(marketId))) {
                            runningMarkets.add(marketId);
                        }
                    }
                }

                for (String marketId : runningMarkets) {
                    try {
                        ApiNGJsonRpcDemo.cancelOrders(marketId);
                        boolean done = ApiNGJsonRpcDemo.bookProfitOrBackout(marketId);
                        if (done) {
                            runningMarkets.remove(marketId);
                            bookedMarkets.add(marketId);
                        }
                    } catch (Throwable e) {
                        logger.error(e.getMessage()+", Message Id: "+marketId);
                        e.printStackTrace();
                    }
                }

                logger.info("Sleeping ProfitOrLossBookingTread");
                Thread.sleep(5000);
            } catch (InterruptedException e) {

                e.printStackTrace();
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
                //ignore
            } catch (Throwable e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
