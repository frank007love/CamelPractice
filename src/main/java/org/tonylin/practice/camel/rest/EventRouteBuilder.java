package org.tonylin.practice.camel.rest;

import static com.google.common.base.Preconditions.checkState;

import org.apache.camel.builder.RouteBuilder;

public class EventRouteBuilder extends RouteBuilder {

	private final static String GET_EVENTS = "GET_EVENTS";
	
	private Object eventHandler;
	
	public EventRouteBuilder(Object eventHandler) {
		this.eventHandler = eventHandler;

	}
	
	@Override
	public void configure() throws Exception {
		checkState(eventHandler!=null, "Can't find eventHandler");
		
		rest("/events/{id}").get().route().id(GET_EVENTS).bean(eventHandler).endRest();
	}
}
