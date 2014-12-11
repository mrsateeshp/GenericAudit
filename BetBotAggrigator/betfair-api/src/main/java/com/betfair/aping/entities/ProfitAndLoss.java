package com.betfair.aping.entities;



public class ProfitAndLoss {

	private long selectionId;
	private double ifWin;

	public long getSelectionId() {
		return selectionId;
	}

	public void setSelectionId(long selectionId) {
		this.selectionId = selectionId;
	}

	public double getIfWin() {
		return ifWin;
	}

	public void setIfWin(double ifWin) {
		this.ifWin = ifWin;
	}
}
