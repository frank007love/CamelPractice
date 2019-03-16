package org.tonylin.practice.camel.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestHandler {
	private static Logger logger = LoggerFactory.getLogger(RestHandler.class);
	private List<String> requestIds = new ArrayList<String>();
	
	@Handler
	public void handle(Exchange exchange) {
		String requestId = exchange.getIn().getHeader("id", String.class);
		logger.debug("Request id: {}", requestId);
		requestIds.add(requestId);
	}
	
	public List<String> getRequestIds(){
		return requestIds;
	}
}
