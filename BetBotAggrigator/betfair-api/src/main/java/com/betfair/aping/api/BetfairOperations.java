package com.betfair.aping.api;

import com.betfair.aping.MarketAtInPlay;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.MarketProjection;
import com.betfair.aping.enums.MarketSort;
import com.betfair.aping.enums.MatchProjection;
import com.betfair.aping.enums.OrderProjection;
import com.betfair.aping.exceptions.APINGException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public interface BetfairOperations {
	public static final String FILTER = "filter";
    public static final String LOCALE = "locale";
    public static final String SORT = "sort";
    public static final String MAX_RESULT = "maxResults";
    public static final String MARKET_IDS = "marketIds";
    public static final String MARKET_ID = "marketId";
    public static final String INSTRUCTIONS = "instructions";
    public static final String CUSTOMER_REF = "customerRef";
    public static final String MARKET_PROJECTION = "marketProjection";
    public static final String PRICE_PROJECTION = "priceProjection";
    public static final String MATCH_PROJECTION = "matchProjection";
    public static final String ORDER_PROJECTION = "orderProjection";
    public static final String locale = Locale.getDefault().toString();

	public List<EventTypeResult> listEventTypes(MarketFilter filter) throws APINGException;

	public  List<MarketBook> listMarketBook(List<String> marketIds, PriceProjection priceProjection, OrderProjection orderProjection,
						MatchProjection matchProjection, String currencyCode) throws APINGException;

    public  MarketBook getMarketBook(String marketId) throws APINGException;

    public  List<MarketCatalogue> listMarketCatalogue(MarketFilter filter, Set<MarketProjection> marketProjection,
        MarketSort sort, String maxResult) throws APINGException;

	public  PlaceExecutionReport placeOrders(String marketId, List<PlaceInstruction> instructions, String customerRef ) throws APINGException;

    public  boolean cancelOrders(String marketId, List<CancelInstruction> cancelInstructions) throws APINGException;

    public   List<Order> listCurrentOrders(String marketId, OrderProjection orderProjection) throws APINGException ;

    public   ProfitAndLossReport listMarketProfitAndLoss(String marketId) throws APINGException ;

    public  String makeRequest(String operation, Map<String, Object> params) throws  APINGException;

    public  List<MarketAtInPlay> getNewInPlayMarkets() throws  APINGException;
}

