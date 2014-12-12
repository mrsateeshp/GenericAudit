package com.betfair.aping.entities;

import java.io.Serializable;

public class Error  implements Serializable {

	private Data data;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

}
