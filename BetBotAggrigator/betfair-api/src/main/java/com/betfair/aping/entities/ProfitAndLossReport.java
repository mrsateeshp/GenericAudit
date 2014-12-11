package com.betfair.aping.entities;

import com.betfair.aping.enums.ExecutionReportErrorCode;
import com.betfair.aping.enums.ExecutionReportStatus;

import java.util.List;


public class ProfitAndLossReport {

	private String marketId;
	private List<ProfitAndLoss> profitAndLosses;

	public String getMarketId() {
		return marketId;
	}

	public void setMarketId(String marketId) {
		this.marketId = marketId;
	}

	public List<ProfitAndLoss> getProfitAndLosses() {
		return profitAndLosses;
	}

	public void setProfitAndLosses(List<ProfitAndLoss> profitAndLosses) {
		this.profitAndLosses = profitAndLosses;
	}
}
