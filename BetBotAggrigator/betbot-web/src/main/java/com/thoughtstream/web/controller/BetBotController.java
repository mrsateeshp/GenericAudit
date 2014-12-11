package com.thoughtstream.web.controller;

import com.betfair.aping.ApiNGJsonRpcDemo;
import com.betfair.aping.SportRiskProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class BetBotController {

	static final Logger logger = LoggerFactory.getLogger(BetBotController.class);
	
	BetBotTread betbotThread = null;
	ProfitOrLossBookingTread profitOrLossBookingTread;
	private final String DEFAULT_APP_KEY = "ac87jtGotn880Lx6";


	@RequestMapping(value = { "/start" }, method = RequestMethod.GET)
	public ModelAndView startBetBot(@RequestParam(required = false, defaultValue = DEFAULT_APP_KEY) String appKey,
									@RequestParam(defaultValue = "") String sessionToken,
									@RequestParam(defaultValue = "1.2") double upperEndOfOdds,
									@RequestParam(defaultValue = "1.15") double lowerEndOfOdds,
									@RequestParam(defaultValue = "0.5") double riskFactor,
									@RequestParam(defaultValue = "0.80") double profitFactor,
									@RequestParam(defaultValue = "0.70") double giveUpFactor,
									@RequestParam(defaultValue = "") String sport,
									@RequestParam(defaultValue = "12") double amount,
									@RequestParam(defaultValue = "100000") int minMatched
	)
	throws Exception{
		ApiNGJsonRpcDemo.applicationKey = appKey;

		if(!sessionToken.equals("")){
			ApiNGJsonRpcDemo.sessionToken = sessionToken;
		}
		if(!sport.equals("")){
			SportRiskProfile sportsStrategy = SportRiskProfile.getSportsStrategy(sport);
			sportsStrategy.upperEndOfOdds = upperEndOfOdds;
			sportsStrategy.lowerEndOfOdds = lowerEndOfOdds;
			sportsStrategy.riskFactor = riskFactor;
			sportsStrategy.profitFactor = profitFactor;
			sportsStrategy.giveUpFactor = giveUpFactor;
		}

		ApiNGJsonRpcDemo.amount = amount;
		ApiNGJsonRpcDemo.minMatched = minMatched;

		if(betbotThread == null) {
			betbotThread = new BetBotTread();
			betbotThread.start();
		}

		if(profitOrLossBookingTread==null){
			profitOrLossBookingTread = new ProfitOrLossBookingTread();
			profitOrLossBookingTread.start();
		}

		return getBetBotStatus();
	}

	@RequestMapping(value = { "/stop" }, method = RequestMethod.GET)
	public ModelAndView stopBetBot()
			throws Exception{
		if(betbotThread!=null && betbotThread.getState() != Thread.State.TERMINATED && betbotThread.getState() != Thread.State.NEW) {
			betbotThread.terminate();
			while (betbotThread.getState() != Thread.State.TERMINATED){
				logger.debug("waiting for the thread to stop!!!");
				Thread.sleep(5000);
			}
		}

		betbotThread = null;

		return getBetBotStatus();
	}
	@RequestMapping(value = { "/status" }, method = RequestMethod.GET)
	public ModelAndView getBetBotStatus() {
		ModelAndView model = new ModelAndView();
		model.addObject("title", "BetBot Status");
		String message = "No Message";
		if(betbotThread != null) {
			message = betbotThread.getState().toString();
		}else {
			message = "Betbot Not initialized!!!";
		}
		model.addObject("message", message);
		model.setViewName("status");
		return model;
	}

	@RequestMapping(value = { "/", "/welcome**" }, method = RequestMethod.GET)
	public ModelAndView welcomePage() {

		ModelAndView model = new ModelAndView();
		model.addObject("title", "Spring Security Hello World");
		model.addObject("message", "This is welcome page!");
		model.setViewName("hello");
		return model;

	}

	@RequestMapping(value = "/admin**", method = RequestMethod.GET)
	public ModelAndView adminPage() {

		ModelAndView model = new ModelAndView();
		model.addObject("title", "Spring Security Hello World");
		model.addObject("message", "This is protected page!");
		model.setViewName("admin");

		return model;

	}

}