package org.tonylin.practice.camel.throttler;

import static com.google.common.base.Preconditions.checkState;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.ThrottlerRejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrottlerRouteBuilder extends RouteBuilder {
	private static Logger logger = LoggerFactory.getLogger(ThrottlerRouteBuilder.class);
	private final static String GET_EVENTS = "GET_EVENTS";
	
	private Object eventHandler;
	
	private int limit = 2;
	private int period = 200;
	
	public ThrottlerRouteBuilder(Object eventHandler) {
		this.eventHandler = eventHandler;

	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public void setPeriod(int period) {
		this.period = period;
	}
	
	@Override
	public void configure() throws Exception {
		checkState(eventHandler!=null, "Can't find eventHandler");
		
		onException(ThrottlerRejectedExecutionException.class)
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				logger.debug("handle ThrottlerRejectedExecutionException");
				exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, "503");
			}
		})
		.handled(true);
		
		rest("/events/{id}").get().route().id(GET_EVENTS)
		.throttle(limit)
		.timePeriodMillis(period)
		.rejectExecution(true)
		.bean(eventHandler).endRest();
	}
}
