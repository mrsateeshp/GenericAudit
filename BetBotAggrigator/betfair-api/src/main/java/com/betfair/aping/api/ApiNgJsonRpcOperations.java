package com.betfair.aping.api;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.containers.*;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.*;
import com.betfair.aping.exceptions.APINGException;
import com.betfair.aping.util.JsonConverter;
import com.betfair.aping.util.JsonrpcRequest;

import java.util.*;


public class ApiNgJsonRpcOperations extends ApiNgOperations{

    private static ApiNgJsonRpcOperations instance = null;

    private ApiNgJsonRpcOperations(){}

    public static ApiNgJsonRpcOperations getInstance(){
        if (instance == null){
            instance = new ApiNgJsonRpcOperations();
        }
        return instance;
    }

    public List<EventTypeResult> listEventTypes(MarketFilter filter, String appKey, String ssoId) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(FILTER, filter);
        params.put(LOCALE, locale);
        String result = getInstance().makeRequest(ApiNgOperation.LISTEVENTTYPES.getOperationName(), params, appKey, ssoId);
        if(ApiNGDemo.isDebug())
            System.out.println("\nResponse: "+result);

        EventTypeResultContainer container = JsonConverter.convertFromJson(result, EventTypeResultContainer.class);
        if(container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();

    }

    public List<MarketBook> listMarketBook(List<String> marketIds, PriceProjection priceProjection, OrderProjection orderProjection,
                                           MatchProjection matchProjection, String currencyCode, String appKey, String ssoId) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_IDS, marketIds);
        params.put(PRICE_PROJECTION, priceProjection);
        params.put(ORDER_PROJECTION, orderProjection);
        params.put(MATCH_PROJECTION, matchProjection);
        params.put("currencyCode", currencyCode);
        String result = getInstance().makeRequest(ApiNgOperation.LISTMARKETBOOK.getOperationName(), params, appKey, ssoId);
        if(ApiNGDemo.isDebug())
            System.out.println("\nResponse: "+result);

        ListMarketBooksContainer container = JsonConverter.convertFromJson(result, ListMarketBooksContainer.class);

        if(container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();


    }

    public List<MarketCatalogue> listMarketCatalogue(MarketFilter filter, Set<MarketProjection> marketProjection,
                                                     MarketSort sort, String maxResult, String appKey, String ssoId) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(FILTER, filter);
        params.put(SORT, sort);
        params.put(MAX_RESULT, maxResult);
        params.put(MARKET_PROJECTION, marketProjection);
        String result = getInstance().makeRequest(ApiNgOperation.LISTMARKETCATALOGUE.getOperationName(), params, appKey, ssoId);
        if(ApiNGDemo.isDebug())
            System.out.println("\nResponse: "+result);

        ListMarketCatalogueContainer container = JsonConverter.convertFromJson(result, ListMarketCatalogueContainer.class);

        if(container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();

    }

    public PlaceExecutionReport placeOrders(String marketId, List<PlaceInstruction> instructions, String customerRef , String appKey, String ssoId) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_ID, marketId);
        params.put(INSTRUCTIONS, instructions);
        params.put(CUSTOMER_REF, customerRef);
        String result = getInstance().makeRequest(ApiNgOperation.PLACORDERS.getOperationName(), params, appKey, ssoId);
        if(ApiNGDemo.isDebug())
            System.out.println("\nResponse: "+result);

        PlaceOrdersContainer container = JsonConverter.convertFromJson(result, PlaceOrdersContainer.class);

        if(container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();

    }

    @Override
    public boolean cancelOrders(String marketId, List<CancelInstruction> cancelInstructions, String appKey, String ssoId) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_ID, marketId);
        params.put(INSTRUCTIONS, cancelInstructions);
        params.put(CUSTOMER_REF, "1");

        String result = getInstance().makeRequest(ApiNgOperation.CANCELORDERS.getOperationName(), params, appKey, ssoId);
        if(ApiNGDemo.isDebug())
            System.out.println("\nResponse: "+result);

        return result.contains("SUCCESS");
    }

    @Override
    public List<Order> listCurrentOrders(String marketId, OrderProjection orderProjection, String appKey, String ssoId) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_ID, marketId);
        params.put(ORDER_PROJECTION, orderProjection);

        String result = getInstance().makeRequest(ApiNgOperation.LISTCURRENTORDERS.getOperationName(), params, appKey, ssoId);
        if(ApiNGDemo.isDebug())
            System.out.println("\nResponse: "+result);

        ListCurrentOrdersContainer container = JsonConverter.convertFromJson(result, ListCurrentOrdersContainer.class);

        if(container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult().getCurrentOrders();
    }

    @Override
    public ProfitAndLossReport listMarketProfitAndLoss(String marketId, String appKey, String ssoId) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_IDS, Arrays.asList(marketId));

        String result = getInstance().makeRequest(ApiNgOperation.LISTMARKETPROFITANDLOSS.getOperationName(), params, appKey, ssoId);
        if(ApiNGDemo.isDebug())
            System.out.println("\nResponse: "+result);

        ProfitAndLossContainer container = JsonConverter.convertFromJson(result, ProfitAndLossContainer.class);

        if(container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult().get(0);
    }

    protected String makeRequest(String operation, Map<String, Object> params, String appKey, String ssoToken) {
        String requestString;
        //Handling the JSON-RPC request
        JsonrpcRequest request = new JsonrpcRequest();
        request.setId("1");
        request.setMethod(ApiNGDemo.getProp().getProperty("SPORTS_APING_V1_0") + operation);
        request.setParams(params);

        requestString =  JsonConverter.convertToJson(request);
        if(ApiNGDemo.isDebug())
            System.out.println("\nRequest: "+requestString);

        //We need to pass the "sendPostRequest" method a string in util format:  requestString
        HttpUtil requester = new HttpUtil();
        return requester.sendPostRequestJsonRpc(requestString, operation, appKey, ssoToken);

       }



}

