<%@page session="false"%>
<html>
<body>
	<h1>Title : ${title}</h1>	
	<h1>Message : ${message}</h1>

	/logs/betbot.log
	http://localhost:8080/status
	http://localhost:8080/start?sessionToken=ZENO8pkAYXa4G/7fAAAWYrTkjGC5tpOJkVD8WNe3Cqg=
	htt
		public ModelAndView startBetBot(@RequestParam(required = false, defaultValue = DEFAULT_APP_KEY) String appKey,
    									@RequestParam(defaultValue = "") String sessionToken,
    									@RequestParam(defaultValue = "1.3") double upperEndOfOdds,
    									@RequestParam(defaultValue = "1.15") double lowerEndOfOdds,
    									@RequestParam(defaultValue = "0.5") double riskFactor,
    									@RequestParam(defaultValue = "0.80") double profitFactor,
    									@RequestParam(defaultValue = "0.90") double giveUpFactor,
    									@RequestParam(defaultValue = "6") double amount,
    									@RequestParam(defaultValue = "100000") int minMatched
    	)p://localhost:8080/stop
</body>
</html>