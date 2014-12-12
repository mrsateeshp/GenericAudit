package com.betfair.aping.entities;

import com.betfair.aping.exceptions.APINGException;

import java.io.Serializable;

public class Data  implements Serializable {

	private APINGException APINGException;

	public APINGException getAPINGException() {
		return APINGException;
	}

	public void setAPINGException(APINGException aPINGException) {
		APINGException = aPINGException;
	}

}
