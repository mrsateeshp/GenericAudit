package com.betfair.aping.containers;

import com.betfair.aping.entities.CurrentOrdersReport;
import com.betfair.aping.entities.Order;

import java.util.List;

public class ListCurrentOrdersContainer extends Container {

	private CurrentOrdersReport result;

	public CurrentOrdersReport getResult() {
		return result;
	}

	public void setResult(CurrentOrdersReport result) {
		this.result = result;
	}

}
