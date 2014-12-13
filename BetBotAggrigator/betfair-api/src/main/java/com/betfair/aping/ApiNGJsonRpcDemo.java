package com.betfair.aping;

import com.betfair.aping.api.BetfairJsonRpcOperations;
import com.betfair.aping.api.BetfairOperations;
import com.betfair.aping.containers.SSOTokenContainer;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.*;
import com.betfair.aping.exceptions.APINGException;
import com.betfair.aping.util.JsonConverter;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
/**
 * This is a demonstration class to show a quick demo of the new Betfair API-NG.
 * When you execute the class will: <li>find a market (next horse race in the
 * UK)</li> <li>get prices and runners on this market</li> <li>place a bet on 1
 * runner</li> <li>handle the error</li>
 *
 */
public class ApiNGJsonRpcDemo {
   /* static class MarketAtInPlay {
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

    static final Logger logger = LoggerFactory.getLogger(ApiNGJsonRpcDemo.class);


    public volatile static String applicationKey="-------------";
    public volatile static String sessionToken = getSSOToken();

    private static BetfairOperations jsonOperations = new BetfairJsonRpcOperations(applicationKey, sessionToken);

    public volatile static double veryHotPlayerOdds = 1.4;
    public volatile static double okPlayerOdds = 1.7;
    public volatile static double amount = 2;
    public volatile static int minMatched = 100000;

    public static volatile Set<String> placedMarkets = Collections.synchronizedSet(new HashSet<String>());
    public static volatile Map<String,MarketAtInPlay> marketsAtInPlay = new HashMap<String,MarketAtInPlay>();
    public static Long customerRef = 1l;

    public static volatile Set<String> avoidSports = Collections.synchronizedSet(new HashSet<String>());
    public static volatile Set<String> avoidSportsCompletely = Collections.synchronizedSet(new HashSet<String>());
    static {
        avoidSportsCompletely.add("Snooker");
        avoidSports.add("Basketball");
        avoidSports.add("Tennis");
    }

    public static String getCustomerRef(){
        customerRef = customerRef + 1;

        return customerRef.toString();
    }

    public static Double getExposure(ProfitAndLossReport report) {
        for (ProfitAndLoss pl : report.getProfitAndLosses()) {
            if(pl.getIfWin() < 0){
                return pl.getIfWin()*-1;
            }
        }
        throw new RuntimeException("Illigal state!!!");
    }

    public static ProfitAndLoss getProfit(ProfitAndLossReport report) {
        for (ProfitAndLoss pl : report.getProfitAndLosses()) {
            if(pl.getIfWin() > 0){
                return pl;
            }
        }
        throw new RuntimeException("Illigal state!!!");
    }

    public static Runner getTradedRunner(MarketBook marketBook, long selectionId){
        for(Runner runner: marketBook.getRunners()) {
            if(runner.getSelectionId() == selectionId){
                return runner;
            }
        }
        throw new RuntimeException("Illigal state!!!");
    }

    public static double getNextLayBet(Double currentOdds) {
        currentOdds = currentOdds + 0.02;
        if(currentOdds <1.99){
            return currentOdds;
        } else if(currentOdds < 6) {
            if((currentOdds*100) % 10 == 0){
                return currentOdds;
            }
            currentOdds = currentOdds * 10;
            int temp = currentOdds.intValue();
            temp += 1;
            return temp/10d;
        } else {
            throw new RuntimeException(currentOdds +" are too high, not supported...");
        }
    }


    public static boolean bookProfitOrBackout(String marketId) throws APINGException {
        return bookProfitOrBackout(marketId, false);
    }

    public static boolean bookProfitOrBackout(String marketId, boolean hardLine) throws APINGException {
        SportRiskProfile sportsStrategy = SportRiskProfile.getSportsStrategy(marketsAtInPlay.get(marketId).getMarketCatalogue().getEventType().getName());
        double riskFactor = sportsStrategy.riskFactor;
        double giveUpFactor= sportsStrategy.giveUpFactor;
        double profitFactor=sportsStrategy.profitFactor;

        boolean result = false;
        ProfitAndLossReport profitAndLossReport = jsonOperations.listMarketProfitAndLoss(marketId, applicationKey, sessionToken);

        double exposure = getExposure(profitAndLossReport);
        ProfitAndLoss profitRunner = getProfit(profitAndLossReport);
        double profit = profitRunner.getIfWin();
        double orgOdds = 1 + round((profitRunner.getIfWin() / exposure));

        PriceProjection priceProjection = new PriceProjection();
        Set<PriceData> priceData = new HashSet<PriceData>();
        priceData.add(PriceData.EX_BEST_OFFERS);
        priceProjection.setPriceData(priceData);

        List<String> marketIds = new ArrayList<String>();
        marketIds.add(marketId);

        List<MarketBook> marketBookReturn = jsonOperations.listMarketBook(marketIds, priceProjection,
                null, null, null, applicationKey, sessionToken);
        MarketBook marketBook = marketBookReturn.get(0);
        Runner tradedRunner = getTradedRunner(marketBook, profitRunner.getSelectionId());
        double currentOdds = tradedRunner.getLastPriceTraded();

//        double currentOdds = 1.6;
        if(currentOdds > orgOdds) {
            logger.info("trading LOW");
            currentOdds = getNextLayBet(currentOdds);
            double layAmount = round((exposure + profit) / currentOdds);
            double layBetLoss = round((currentOdds - 1)*layAmount);
            double loss = (profit - layBetLoss)*-1;

            double riskLevel = round(exposure * riskFactor);
            if(loss > riskLevel){
                logger.info("TAKE ACTION!!!");
                logger.info("Loss -" + loss + " is  looking bad compared to profit: " + profit);

                if(loss > exposure*giveUpFactor){
                    logger.info("Giving up as loss seems to very high, does not make sense to cover it!!");
                }else {
                    if(!hardLine){
                        try {
                            Thread.sleep(7000l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        result = bookProfitOrBackout(marketId, true);
                    }else {
                        logger.info("Placing the lay bet!!");
                        result = placingLayBet(marketBook, tradedRunner, layAmount, currentOdds);
                    }
                }

            }else{
                logger.info("Loss -" + loss + " is  not looking that bad compared to profit: " + profit);
            }

        } else {
            logger.info("trading HIGH");
            currentOdds = currentOdds + 0.01;
            double layAmount = round((exposure + profit)/currentOdds);
            double layBetLoss = round(layAmount*(currentOdds - 1));

            double netProfit = (profit - layBetLoss);

            double profitThreshold = profit * profitFactor;

            if(orgOdds < 1.1){
                profitThreshold = profit;
            }

            if((currentOdds <= 1.05 && orgOdds > 1.1) || netProfit > profitThreshold){
                logger.info("PROFIT::: TAKE ACTION!!!");
                logger.info("NetProfit " + netProfit + " is  looking good compared to profit: " + profit);
                logger.info("Placing the lay bet!!");
                result = placingLayBet(marketBook,tradedRunner,layAmount,currentOdds);
            }else{
                logger.info("NetProfit " + netProfit + " is  not looking that good compared to profit: " + profit);
            }

        }

        return result;
    }
    public static double round(double value) {
        int places = 2;
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static boolean placingLayBet(MarketBook marketBook, Runner runner, double amount, double price) throws APINGException {
        price = round(price);
        List<PlaceInstruction> instructions = new ArrayList<PlaceInstruction>();
        PlaceInstruction instruction = new PlaceInstruction();
        instruction.setHandicap(0);
        instruction.setSide(Side.LAY);
        instruction.setOrderType(OrderType.LIMIT);

        LimitOrder limitOrder = new LimitOrder();
        limitOrder.setPersistenceType(PersistenceType.PERSIST);
        //API-NG will return an error with the default size=0.01. This is an expected behaviour.
        //You can adjust the size and price value in the "apingdemo.properties" file

        limitOrder.setPrice(round(price));
        limitOrder.setSize(amount);

        instruction.setLimitOrder(limitOrder);
        instruction.setSelectionId(runner.getSelectionId());
        instructions.add(instruction);

        String customerRef = getCustomerRef();

        String marketId = marketBook.getMarketId();
        PlaceExecutionReport placeBetResult = jsonOperations.placeOrders(marketId, instructions, customerRef, applicationKey, sessionToken);

        // Handling the operation result
        if (placeBetResult.getStatus() == ExecutionReportStatus.SUCCESS) {
            logger.info("Your bet has been placed!!");

            logger.info(placeBetResult.getInstructionReports().toString());
            double sizeMatched = 0;
            for(PlaceInstructionReport report: placeBetResult.getInstructionReports()){
                sizeMatched += report.getSizeMatched();
            }
            if(amount != sizeMatched){
                logger.error("Bet did not fully match: "+placeBetResult.getInstructionReports().toString());
                logger.error("Bet did not fully match: selection Id: "+marketBook.getRunners().get(0).getSelectionId() +" ,"+marketBook.getRunners().get(1).getSelectionId());
            }

            return !cancelOrders(marketId);
        } else if (placeBetResult.getStatus() == ExecutionReportStatus.FAILURE) {
            logger.error("Your bet has NOT been placed :*( amount: "+amount+" odds: "+ price);
            logger.error("The error is: " + placeBetResult.getErrorCode() + ": " + placeBetResult.getErrorCode().getMessage());
            return false;
        }

        return false;
    }

    public static boolean isMarketClosed(String marketId) throws APINGException {

        PriceProjection priceProjection = new PriceProjection();
        Set<PriceData> priceData = new HashSet<PriceData>();
        priceData.add(PriceData.EX_BEST_OFFERS);
        priceProjection.setPriceData(priceData);

        List<String> marketIds = new ArrayList<String>();
        marketIds.add(marketId);

        List<MarketBook> marketBookReturn = jsonOperations.listMarketBook(marketIds, priceProjection,
                null, null, null, applicationKey, sessionToken);
        if(marketBookReturn.isEmpty()){
            return true;
        }
        MarketBook marketBook = marketBookReturn.get(0);
        return marketBook.getStatus().equalsIgnoreCase("CLOSED");
    }

    public static boolean cancelOrders(String marketId) throws APINGException {
        List<Order> orders = jsonOperations.listCurrentOrders(marketId, OrderProjection.EXECUTABLE, applicationKey, sessionToken);

        if(orders.isEmpty()){
            return false;
        }

        for(Order order: orders){
            CancelInstruction instruction = new CancelInstruction();
            instruction.setBetId(order.getBetId());
            instruction.setSizeReduction(order.getSize());
            List<CancelInstruction> instructions = new ArrayList<CancelInstruction>();
            instructions.add(instruction);
            jsonOperations.cancelOrders(marketId, instructions, applicationKey, sessionToken);
        }

        return true;
    }

    public void start() {

        try {

            *//**
             * ListEventTypes: Search for the event types and then for the "Horse Racing" in the returned list to finally get
             * the listEventTypeId
             *//*
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

            List<MarketCatalogue> marketCatalogueResult = jsonOperations.listMarketCatalogue(marketFilter, marketProjection, MarketSort.FIRST_TO_START, maxResults,
                    applicationKey, sessionToken);
            logger.info("$$$$$ No of markets found: " + marketCatalogueResult.size());

            for(MarketCatalogue marketCatalogue : marketCatalogueResult) {

                if(avoidSportsCompletely.contains(marketCatalogue.getEventType().getName())){
                    //
                    continue;
                }


//                logger.info("5. Print static marketId, name and runners....\n");
                printMarketCatalogue(marketCatalogue);
                *//**
                 * ListMarketBook: get list of runners in the market, parameters:
                 * marketId:  the market we want to list runners
                 *
                 *//*
//                logger.info("6.(listMarketBook) Get volatile info for Market including best 3 exchange prices available...\n");
                String marketIdChosen = marketCatalogue.getMarketId();

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

                List<MarketBook> marketBookReturn = jsonOperations.listMarketBook(marketIds, priceProjection,
                        orderProjection, matchProjection, currencyCode, applicationKey, sessionToken);


                *//**
                 * PlaceOrders: we try to place a bet, based on the previous request we provide the following:
                 * marketId: the market id
                 * selectionId: the runner selection id we want to place the bet on
                 * side: BACK - specify side, can be Back or Lay
                 * orderType: LIMIT - specify order type
                 * size: the size of the bet
                 * price: the price of the bet
                 * customerRef: 1 - unique reference for a transaction specified by user, must be different for each request
                 *
                 *//*

                long selectionId = 0;
                if (marketBookReturn.size() != 0) {
                    MarketBook marketBook =  marketBookReturn.get(0);


                    boolean newMarket = addMarketToMarketAtInPlay(marketCatalogue, marketBook);

                    if ((marketBook.getNumberOfRunners() >= 2 || marketBook.getNumberOfRunners() <= 3) && !placedMarkets.contains(marketIdChosen) && marketBook.getTotalMatched() > minMatched) {

                        Runner runner1 = marketBook.getRunners().get(0);
                        Runner runner2 = marketBook.getRunners().get(1);
                        Runner runner3 = runner2;
                        if(marketBook.getRunners().size() == 3) {
                            runner3 = marketBook.getRunners().get(2);
                        }

                        if(newMarket){
                            Runner runner;

                            if (runner1.getLastPriceTraded() <= veryHotPlayerOdds) {
                                runner = runner1;
                            } else if (runner2.getLastPriceTraded() <= veryHotPlayerOdds) {
                                runner = runner2;
                            } else if (runner3.getLastPriceTraded() <= veryHotPlayerOdds) {
                                runner = runner3;
                            }else {
                                continue;
                            }

                            placeBackBet(runner,marketCatalogue);
                            continue;
                        }

                        if(avoidSports.contains(marketCatalogue.getEventType().getName()) && !isMarketHotAtStart(marketIdChosen)){
                            //
                            continue;
                        }

                        SportRiskProfile sportsStrategy = SportRiskProfile.getSportsStrategy(marketsAtInPlay.get(marketIdChosen).getMarketCatalogue().getEventType().getName());
                        double upperEndOfOdds=sportsStrategy.upperEndOfOdds;
                        logger.info("upperEndOfOdds for the market: "+marketCatalogue.getMarketName()+ " is: "+upperEndOfOdds);
                        logger.info("********TOTOAL Matched*********" + marketBook.getTotalMatched());
                        if (runner1.getLastPriceTraded() <= upperEndOfOdds || runner2.getLastPriceTraded() <= upperEndOfOdds || runner3.getLastPriceTraded() <= upperEndOfOdds) {
                            Runner runner = null;
                            if (runner1.getLastPriceTraded() <= upperEndOfOdds) {
                                runner = runner1;
                            } else if (runner2.getLastPriceTraded() <= upperEndOfOdds) {
                                runner = runner2;
                            } else {
                                runner = runner3;
                            }

                            MarketAtInPlay marketAtInPlay = marketsAtInPlay.get(marketIdChosen);
                            Runner favRunnerAtStart = getFavRunnerAtMarketStart(marketAtInPlay.getMarketBook().getRunners());
                            if(favRunnerAtStart == null){
                                logger.info("No fav runner at start, so ignoring the market...");
                            } else {
                                if(favRunnerAtStart.getSelectionId().equals(runner.getSelectionId())){
                                    logger.info("placing the bet as Current fav runner is same as the one at Market start...");
                                    placeBackBet(runner, marketCatalogue);
                                } else {
                                    logger.info("Current fav runner is not same as the one at Market start, so ignoring it...");
                                }
                            }

                        } else {
                            logger.info(String.format("conditions did not match! prices(%f, %f, %f)", runner1.getLastPriceTraded(), runner2.getLastPriceTraded(), runner3.getLastPriceTraded()));
                        }
                    }else {
                       logger.info("********TOTOAL DID not Matched*********" + marketBook.getTotalMatched());
                    }
                } else {
                    logger.info("Sorry, no runners found\n\n");
                }
            }

        } catch (APINGException apiExc) {
            logger.info("Emailable Exception: " + apiExc.toString());
            if(apiExc.toString().contains("INVALID_SESSION_INFORMATION")) {
                try {
                    String ssoToken = getSSOToken();
                    logger.info("new SSO Token generated: "+sessionToken);
                    sessionToken = ssoToken;
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                    sendEmail("failed to generate token: " + e.getMessage());
                }
            }

        }
    }

    public static boolean isMarketHotAtStart(String marketId){
        MarketAtInPlay marketAtInPlay = marketsAtInPlay.get(marketId);
        MarketBook marketBook = marketAtInPlay.getMarketBook();
        Runner runner1 = marketBook.getRunners().get(0);
        Runner runner2 = marketBook.getRunners().get(1);
        Runner runner3 = runner2;
        if(marketBook.getRunners().size() == 3) {
            runner3 = marketBook.getRunners().get(2);
        }

        Runner runner;

        if (runner1.getLastPriceTraded() <= veryHotPlayerOdds) {
            return true;
        } else if (runner2.getLastPriceTraded() <= veryHotPlayerOdds) {
            return true;
        } else if (runner3.getLastPriceTraded() <= veryHotPlayerOdds) {
            return true;
        }else {
            return false;
        }
    }

    public Runner getFavRunner(List<Runner> runners){
        Runner result = null;
        for(Runner runner: runners){
            if(result == null){
                result = runner;
            }else if(runner.getLastPriceTraded() < result.getLastPriceTraded()){
                result = runner;
            }
        }

        if(result!=null && result.getLastPriceTraded() <= okPlayerOdds){
            return result;
        } else {
            return null;
        }
    }
    private boolean addMarketToMarketAtInPlay(MarketCatalogue marketCatalogue, MarketBook marketBook) {
        if(!marketsAtInPlay.containsKey(marketBook.getMarketId())){
            marketsAtInPlay.put(marketBook.getMarketId(), new MarketAtInPlay(marketCatalogue, marketBook));
            return true;
        } else {
            return false;
        }
    }

    public void placeBackBet(Runner runner, MarketCatalogue marketCatalogue) throws APINGException {
        placeBackBet(runner, marketCatalogue, false);
    }
    public void placeBackBet(Runner runner, MarketCatalogue marketCatalogue, boolean hardLine) throws APINGException {
        String marketIdChosen = marketCatalogue.getMarketId();
        long selectionId = runner.getSelectionId();
        if(isTheDraw(marketCatalogue, selectionId)){
            //dont trade as it is too risky to trade on draw.
            logger.info("Not trading because the runner is 'The Draw'");
            return;
        }

        logger.info("7. Place a bet below minimum stake to prevent the bet actually " +
                "being placed for marketId: " + marketIdChosen + " with selectionId: " + selectionId + "...\n\n");
        List<PlaceInstruction> instructions = new ArrayList<PlaceInstruction>();
        PlaceInstruction instruction = new PlaceInstruction();
        instruction.setHandicap(0);
        instruction.setSide(Side.BACK);
        instruction.setOrderType(OrderType.LIMIT);

        LimitOrder limitOrder = new LimitOrder();
        limitOrder.setPersistenceType(PersistenceType.PERSIST);
        //API-NG will return an error with the default size=0.01. This is an expected behaviour.
        //You can adjust the size and price value in the "apingdemo.properties" file
        double price = runner.getLastPriceTraded() - .05;

        SportRiskProfile sportsStrategy = SportRiskProfile.getSportsStrategy(marketsAtInPlay.get(marketIdChosen).getMarketCatalogue().getEventType().getName());
        double lowerEndOfOdds=sportsStrategy.lowerEndOfOdds;
        double upperEndOfOdds=sportsStrategy.upperEndOfOdds;

        if(runner.getLastPriceTraded() < lowerEndOfOdds || runner.getLastPriceTraded() > upperEndOfOdds) {
            logger.info("Skipping to place the bet for market "+marketCatalogue.getMarketName() + " as odds changed... new odds: "+runner.getLastPriceTraded());
            //don't trade as it is too risky to trade below 1.1...
            return;
        }

        if(price < 1.01){
            price = 1.01;
        }

        price = round(price);
        logger.info("Placing the bet for market "+marketCatalogue.getMarketName() + " at the odds of: "+price);

        limitOrder.setPrice(price);
        limitOrder.setSize(amount);

        instruction.setLimitOrder(limitOrder);
        instruction.setSelectionId(selectionId);
        instructions.add(instruction);
        if(!hardLine){
            try {
                Thread.sleep(15000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            placeBackBet(runner, marketCatalogue, true);
            return;
        }

        String customerRef = getCustomerRef();

        PlaceExecutionReport placeBetResult = jsonOperations.placeOrders(marketIdChosen, instructions, customerRef, applicationKey, sessionToken);

        // Handling the operation result
        if (placeBetResult.getStatus() == ExecutionReportStatus.SUCCESS) {
            placedMarkets.add(marketIdChosen);
            logger.info("Your bet has been placed!!");

            logger.info(placeBetResult.getInstructionReports().toString());
        } else if (placeBetResult.getStatus() == ExecutionReportStatus.FAILURE) {
            logger.error("NORMAL: Your bet has NOT been placed :*( amount: " + amount + " odds: " + price);
            logger.error("NORMAL: The error is: " + placeBetResult.getErrorCode() + ": " + placeBetResult.getErrorCode().getMessage());
        }
    }

    private boolean isTheDraw(MarketCatalogue marketCatalogue, long selectionId){
        for(RunnerCatalog runnerCatalog: marketCatalogue.getRunners()) {
            if(runnerCatalog.getSelectionId() == selectionId){
                return runnerCatalog.getRunnerName().equalsIgnoreCase("The Draw");
            }
        }
        throw new IllegalStateException("Runner not found!!!");
    }
    private static boolean mailSent = false;


    public static void sendEmail(String strMessage) {
        if(mailSent) return;
        mailSent = true;
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("exp.sqateez@gmail.com","squateez");
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("exp.sqateez@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("sqateez@gmail.com"));
            message.setSubject("BetBott Message!!!");
            message.setText(strMessage);

            Transport.send(message);

            logger.info("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    private static double getPrice() {

        try {
            return new Double((String) ApiNGDemo.getProp().get("BET_PRICE"));
        } catch (NumberFormatException e) {
            //returning the default value
            return new Double(1000);
        }
    }

    private static double getSize(){
        try{
            return new Double((String)ApiNGDemo.getProp().get("BET_SIZE"));
        } catch (NumberFormatException e){
            //returning the default value
            return new Double(0.01);
        }
    }

    private void printMarketCatalogue(MarketCatalogue mk){
        logger.info("Market Name: "+mk.getMarketName() + "; Id: "+mk.getMarketId()+"\n");
        List<RunnerCatalog> runners = mk.getRunners();
        if(runners!=null){
            for(RunnerCatalog rCat : runners){
                logger.info("Runner Name: "+rCat.getRunnerName()+"; Selection Id: "+rCat.getSelectionId()+"\n");
            }
        }
    }

    public static void main(String[] args) throws Exception{

    }

    public static String getSSOToken() throws RuntimeException{
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://identitysso.betfair.com/api/login");

            String USER_AGENT = "Mozilla/5.0";

            post.setHeader("X-Application", applicationKey);
            post.setHeader("Accept", "application/json");
            post.setHeader("User-Agent", USER_AGENT);


            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("username", new String(Base64.decodeBase64("----------------"))));
            urlParameters.add(new BasicNameValuePair("password", new String(Base64.decodeBase64("---------"))));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            SSOTokenContainer container = JsonConverter.convertFromJson(result.toString(), SSOTokenContainer.class);

            if(container.getError().trim().length() > 0){
                throw new RuntimeException("failed to get token, error: "+container.getError());
            }
            return container.getToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
*/}
