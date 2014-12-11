package com.thoughtstream.web.controller;

import com.betfair.aping.*;

/**
 * This is a demonstration class to show a quick demo of the new Betfair API-NG.
 * When you execute the class will: <li>find a market (next horse race in the
 * UK)</li> <li>get prices and runners on this market</li> <li>place a bet on 1
 * runner</li> <li>handle the error</li>
 *
 */
public class BetBotTread extends Thread {

    private volatile boolean running = true;

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {
        final ApiNGJsonRpcDemo jsonRpcDemo = new ApiNGJsonRpcDemo();
        while(running) {
            try {
                jsonRpcDemo.start();
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                running = false;
            } catch (Exception e){
                e.printStackTrace();
                //ignore
            }
        }
    }
}
