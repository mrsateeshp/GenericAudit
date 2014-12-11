package com.betfair.aping.containers;


import com.betfair.aping.entities.ProfitAndLossReport;

import java.util.List;

public class ProfitAndLossContainer extends Container {

	private List<ProfitAndLossReport> result;
	
	public List<ProfitAndLossReport> getResult() {
		return result;
	}
	
	public void setResult(List<ProfitAndLossReport> result) {
		this.result = result;
	}

}
