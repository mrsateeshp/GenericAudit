package com.betfair.aping.entities;

import java.io.Serializable;

public class MarketOnCloseOrder  implements Serializable {
	private double liability;

	public double getLiability() {
		return liability;
	}

	public void setLiability(double liability) {
		this.liability = liability;
	}

}
