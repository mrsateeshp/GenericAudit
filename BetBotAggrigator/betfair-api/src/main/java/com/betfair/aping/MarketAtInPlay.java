package com.betfair.aping;

import com.betfair.aping.entities.MarketBook;
import com.betfair.aping.entities.MarketCatalogue;
import com.betfair.aping.entities.Runner;
import com.betfair.aping.entities.RunnerCatalog;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Market at the point of going into play.
 *
 * @author Sateesh
 * @since 12/12/2014
 */
public class MarketAtInPlay implements Serializable {
    MarketCatalogue marketCatalogue;
    MarketBook marketBook;
    Runner favouriteRunner;
    RunnerCatalog favouriteRunnerCatalog;
    Date startTime;

    public MarketAtInPlay(MarketCatalogue marketCatalogue, MarketBook marketBook) {
        this.marketCatalogue = marketCatalogue;
        this.marketBook = marketBook;
        startTime = new Date();
        favouriteRunner = getFavRunner(marketBook.getRunners());
    }

    public String getMarketId(){
        return marketBook.getMarketId();
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

    public Runner getFavouriteRunner() {
        return favouriteRunner;
    }

    public Runner getFavRunner(List<Runner> runners) {
        Runner result = null;
        for (Runner runner : runners) {
            if (result == null) {
                result = runner;
            } else if (runner.getLastPriceTraded() < result.getLastPriceTraded()) {
                result = runner;
            }
        }

        return result;
    }

    public RunnerCatalog getFavRunnerCatalog() {
        for (RunnerCatalog runnerCatalog : marketCatalogue.getRunners()) {
            if (runnerCatalog.getSelectionId().equals(favouriteRunner.getSelectionId())) {
                return runnerCatalog;
            }
        }
        throw new IllegalStateException("Runner not found!!!");
    }

    public boolean isFavRunnerTheDraw() {
        return favouriteRunnerCatalog.getRunnerName().equalsIgnoreCase("The Draw");
    }
}
