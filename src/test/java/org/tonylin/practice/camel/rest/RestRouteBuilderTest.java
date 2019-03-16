package org.tonylin.practice.camel.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tonylin.practice.camel.rest.RestRouteBuilder;


public class RestRouteBuilderTest extends CamelTestSupport {
	private static Logger logger = LoggerFactory.getLogger(RestRouteBuilderTest.class);
	
	private RestHandler hander = new RestHandler();
	
	
	@Override
	protected RoutesBuilder createRouteBuilder() throws Exception {
		return new RestRouteBuilder(hander);
	}
	
	public static class RestHandler {
		
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
	
	@Test
	public void testHttpGet() throws Exception {
		// when
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet("http://localhost:8080/events/123");
		HttpResponse response = client.execute(httpGet);
		
		// then
		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("123", hander.getRequestIds().get(0));
	}
}
