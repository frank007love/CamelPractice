package org.tonylin.practice.camel.rest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import static com.google.common.base.Preconditions.*;

public class RestRouteBuilder extends RouteBuilder {

	private final static String GET_EVENTS = "GET_EVENTS";
	
	private Object eventHandler;
	
	public RestRouteBuilder(Object eventHandler) {
		this.eventHandler = eventHandler;

	}
	
	@Override
	public void configure() throws Exception {
		checkState(eventHandler!=null, "Can't find eventHandler");
		
		restConfiguration().component("netty4-http").port(8080).bindingMode(RestBindingMode.auto).endpointProperty("ssl",  "false");
		rest("/events/{id}").get().route().id(GET_EVENTS).bean(eventHandler).endRest();
	}
}
