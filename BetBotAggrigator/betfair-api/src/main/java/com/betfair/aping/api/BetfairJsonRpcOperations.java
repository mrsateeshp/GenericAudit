package com.betfair.aping.api;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.MarketAtInPlay;
import com.betfair.aping.SportRiskProfile;
import com.betfair.aping.containers.*;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.*;
import com.betfair.aping.exceptions.APINGException;
import com.betfair.aping.util.JsonConverter;
import com.betfair.aping.util.JsonrpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BetfairJsonRpcOperations implements BetfairOperations {

    static final Logger logger = LoggerFactory.getLogger(BetfairJsonRpcOperations.class);

    private String appKey;
    private String ssoId;

    public BetfairJsonRpcOperations(String appKey, String ssoId) {
        this.appKey = appKey;
        this.ssoId = ssoId;
    }

    public List<EventTypeResult> listEventTypes(MarketFilter filter) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(FILTER, filter);
        params.put(LOCALE, locale);
        String result = makeRequest(ApiNgOperation.LISTEVENTTYPES.getOperationName(), params);
        if (ApiNGDemo.isDebug())
            System.out.println("\nResponse: " + result);

        EventTypeResultContainer container = JsonConverter.convertFromJson(result, EventTypeResultContainer.class);
        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();

    }

    public List<MarketBook> listMarketBook(List<String> marketIds, PriceProjection priceProjection, OrderProjection orderProjection,
                                           MatchProjection matchProjection, String currencyCode) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_IDS, marketIds);
        params.put(PRICE_PROJECTION, priceProjection);
        params.put(ORDER_PROJECTION, orderProjection);
        params.put(MATCH_PROJECTION, matchProjection);
        params.put("currencyCode", currencyCode);
        String result = makeRequest(ApiNgOperation.LISTMARKETBOOK.getOperationName(), params);
        if (ApiNGDemo.isDebug())
            System.out.println("\nResponse: " + result);

        ListMarketBooksContainer container = JsonConverter.convertFromJson(result, ListMarketBooksContainer.class);

        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();
    }

    @Override
    public MarketBook getMarketBook(String marketId) throws APINGException {
        PriceProjection priceProjection = new PriceProjection();
        Set<PriceData> priceData = new HashSet<PriceData>();
        priceData.add(PriceData.EX_BEST_OFFERS);
        priceProjection.setPriceData(priceData);

        return listMarketBook(Arrays.asList(marketId), priceProjection, null,null,null).get(0);
    }

    public List<MarketCatalogue> listMarketCatalogue(MarketFilter filter, Set<MarketProjection> marketProjection,
                                                     MarketSort sort, String maxResult) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(FILTER, filter);
        params.put(SORT, sort);
        params.put(MAX_RESULT, maxResult);
        params.put(MARKET_PROJECTION, marketProjection);
        String result = makeRequest(ApiNgOperation.LISTMARKETCATALOGUE.getOperationName(), params);
        if (ApiNGDemo.isDebug())
            System.out.println("\nResponse: " + result);

        ListMarketCatalogueContainer container = JsonConverter.convertFromJson(result, ListMarketCatalogueContainer.class);

        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();

    }

    public PlaceExecutionReport placeOrders(String marketId, List<PlaceInstruction> instructions, String customerRef) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_ID, marketId);
        params.put(INSTRUCTIONS, instructions);
        params.put(CUSTOMER_REF, customerRef);
        String result = makeRequest(ApiNgOperation.PLACORDERS.getOperationName(), params);
        if (ApiNGDemo.isDebug())
            System.out.println("\nResponse: " + result);

        PlaceOrdersContainer container = JsonConverter.convertFromJson(result, PlaceOrdersContainer.class);

        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();

    }

    @Override
    public boolean cancelOrders(String marketId, List<CancelInstruction> cancelInstructions) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_ID, marketId);
        params.put(INSTRUCTIONS, cancelInstructions);
        params.put(CUSTOMER_REF, "1");

        String result = makeRequest(ApiNgOperation.CANCELORDERS.getOperationName(), params);
        if (ApiNGDemo.isDebug())
            System.out.println("\nResponse: " + result);

        return result.contains("SUCCESS");
    }

    @Override
    public List<Order> listCurrentOrders(String marketId, OrderProjection orderProjection) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_ID, marketId);
        params.put(ORDER_PROJECTION, orderProjection);

        String result = makeRequest(ApiNgOperation.LISTCURRENTORDERS.getOperationName(), params);
        if (ApiNGDemo.isDebug())
            System.out.println("\nResponse: " + result);

        ListCurrentOrdersContainer container = JsonConverter.convertFromJson(result, ListCurrentOrdersContainer.class);

        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult().getCurrentOrders();
    }

    @Override
    public ProfitAndLossReport listMarketProfitAndLoss(String marketId) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_IDS, Arrays.asList(marketId));

        String result = makeRequest(ApiNgOperation.LISTMARKETPROFITANDLOSS.getOperationName(), params);
        if (ApiNGDemo.isDebug())
            System.out.println("\nResponse: " + result);

        ProfitAndLossContainer container = JsonConverter.convertFromJson(result, ProfitAndLossContainer.class);

        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult().get(0);
    }

    public String makeRequest(String operation, Map<String, Object> params) {
        String requestString;
        //Handling the JSON-RPC request
        JsonrpcRequest request = new JsonrpcRequest();
        request.setId("1");
        request.setMethod(ApiNGDemo.getProp().getProperty("SPORTS_APING_V1_0") + operation);
        request.setParams(params);

        requestString = JsonConverter.convertToJson(request);
        if (ApiNGDemo.isDebug())
            System.out.println("\nRequest: " + requestString);

        //We need to pass the "sendPostRequest" method a string in util format:  requestString
        HttpUtil requester = new HttpUtil();
        return requester.sendPostRequestJsonRpc(requestString, operation, appKey, ssoId);

    }

    public static volatile Map<String,MarketAtInPlay> marketsAtInPlay = new HashMap<String,MarketAtInPlay>();

    @Override
    public List<MarketAtInPlay> getNewInPlayMarkets() throws APINGException {
        List<MarketAtInPlay> result = new ArrayList<MarketAtInPlay>();

        /**
         * ListEventTypes: Search for the event types and then for the "Horse Racing" in the returned list to finally get
         * the listEventTypeId
         */
        MarketFilter marketFilter;

        TimeRange time = new TimeRange();
        time.setFrom(new Date());

        Set<String> typesCode = new HashSet<String>();
        typesCode.add("MATCH_ODDS");

        marketFilter = new MarketFilter();
        marketFilter.setMarketTypeCodes(typesCode);
        marketFilter.setInPlayOnly(true);

        Set<MarketProjection> marketProjection = new HashSet<MarketProjection>();
        marketProjection.add(MarketProjection.RUNNER_DESCRIPTION);
        marketProjection.add(MarketProjection.EVENT_TYPE);
        marketProjection.add(MarketProjection.MARKET_START_TIME);

        String maxResults = "200";

        List<MarketCatalogue> marketCatalogueResult = listMarketCatalogue(marketFilter, marketProjection, MarketSort.FIRST_TO_START, maxResults);
        logger.info("$$$$$ No of markets found: " + marketCatalogueResult.size());

        for(MarketCatalogue marketCatalogue : marketCatalogueResult) {

            String marketIdChosen = marketCatalogue.getMarketId();
            if(marketsAtInPlay.containsKey(marketIdChosen)){
                continue;
            }

            PriceProjection priceProjection = new PriceProjection();
            Set<PriceData> priceData = new HashSet<PriceData>();
            priceData.add(PriceData.EX_BEST_OFFERS);
            priceProjection.setPriceData(priceData);

            //In this case we don't need these objects so they are declared null
            OrderProjection orderProjection = null;
            MatchProjection matchProjection = null;
            String currencyCode = null;

            List<String> marketIds = new ArrayList<String>();
            marketIds.add(marketIdChosen);

            List<MarketBook> marketBookReturn = listMarketBook(marketIds, priceProjection,
                    orderProjection, matchProjection, currencyCode);

            long selectionId = 0;
            if (marketBookReturn.size() != 0) {
                MarketBook marketBook =  marketBookReturn.get(0);

                MarketAtInPlay marketAtInPlay1 = new MarketAtInPlay(marketCatalogue, marketBook);
                marketsAtInPlay.put(marketBook.getMarketId(), marketAtInPlay1);
                result.add(marketAtInPlay1);

            } else {
                logger.info("Sorry, no new markets found\n\n");
            }
        }

        return result;
    }
}

