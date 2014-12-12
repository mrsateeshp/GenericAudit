package com.betfair.aping.entities;

import com.betfair.aping.enums.OrderType;
import com.betfair.aping.enums.Side;

import java.io.Serializable;

public class CancelInstruction  implements Serializable {

	private String betId;
	private Double sizeReduction;

	public String getBetId() {
		return betId;
	}

	public void setBetId(String betId) {
		this.betId = betId;
	}

	public Double getSizeReduction() {
		return sizeReduction;
	}

	public void setSizeReduction(Double sizeReduction) {
		this.sizeReduction = sizeReduction;
	}
}
