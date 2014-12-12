package com.betfair.aping.entities;

import com.betfair.aping.entities.EventType;

import java.io.Serializable;

public class EventTypeResult  implements Serializable {
	private EventType eventType ; 
	private int marketCount;
	
	public EventType getEventType() {
		return eventType;
	}
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
	public int getMarketCount() {
		return marketCount;
	}
	public void setMarketCount(int marketCount) {
		this.marketCount = marketCount;
	}

}
