package com.betfair.aping.entities;

import java.io.Serializable;

public class PriceSize implements Serializable{
	private Double price;
	private Double size;

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getSize() {
		return size;
	}

	public void setSize(Double size) {
		this.size = size;
	}

}
