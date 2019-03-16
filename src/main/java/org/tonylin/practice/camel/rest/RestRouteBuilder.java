package org.tonylin.practice.camel.rest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class RestRouteBuilder extends RouteBuilder {	
	
	@Override
	public void configure() throws Exception {
		restConfiguration().component("netty4-http").port(8080).bindingMode(RestBindingMode.auto).endpointProperty("ssl",  "false");
	}
}
