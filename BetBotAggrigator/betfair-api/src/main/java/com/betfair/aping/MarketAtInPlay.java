package com.betfair.aping;

import com.betfair.aping.entities.MarketBook;
import com.betfair.aping.entities.MarketCatalogue;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Sateesh
 * @since 12/12/2014
 */
public class MarketAtInPlay implements Serializable{
    MarketCatalogue marketCatalogue;
    MarketBook marketBook;
    Date startTime;

    public MarketAtInPlay(MarketCatalogue marketCatalogue, MarketBook marketBook) {
        this.marketCatalogue = marketCatalogue;
        this.marketBook = marketBook;
        startTime = new Date();
    }

    public MarketCatalogue getMarketCatalogue() {
        return marketCatalogue;
    }

    public MarketBook getMarketBook() {
        return marketBook;
    }

    public Date getStartTime() {
        return startTime;
    }
}
