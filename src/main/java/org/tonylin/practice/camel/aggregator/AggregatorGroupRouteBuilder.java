package org.tonylin.practice.camel.aggregator;

import static com.google.common.base.Preconditions.checkState;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;

public class AggregatorGroupRouteBuilder extends RouteBuilder {

	private final static String GET_EVENTS = "GET_EVENTS";
	
	private Object eventHandler;
	
	public AggregatorGroupRouteBuilder(Object eventHandler) {
		this.eventHandler = eventHandler;

	}
	
	@Override
	public void configure() throws Exception {
		checkState(eventHandler!=null, "Can't find eventHandler");
		
		rest("/events/{id}").get().route().id(GET_EVENTS)
		.aggregate(new GroupedExchangeAggregationStrategy()).header("id").completionInterval(500)
		.bean(eventHandler).endRest();
	}
}
