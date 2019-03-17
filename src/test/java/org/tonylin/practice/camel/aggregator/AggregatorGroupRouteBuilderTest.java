package org.tonylin.practice.camel.aggregator;

import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.tonylin.practice.camel.rest.RestRouteBuilder;

public class AggregatorGroupRouteBuilderTest extends CamelTestSupport  {

	private RestHandler hander = new RestHandler();
	
	public static class RestHandler {
		private Map<String, List<Exchange>> requestData = new ConcurrentHashMap<String, List<Exchange>>();
		private CountDownLatch countDownLatch;
		
		@SuppressWarnings("unchecked")
		@Handler
		public void handle(Exchange exchange) {
			List<Exchange> groupExchanges = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);
			String id = groupExchanges.get(0).getIn().getHeader("id", String.class);
			requestData.put(id, groupExchanges);
			
			if( countDownLatch != null )
				countDownLatch.countDown();
		}
		
		public Map<String, List<Exchange>> getRequestData(){
			return requestData;
		}
		
		public void expectNum(int num) {
			countDownLatch = new CountDownLatch(num);
		}
		
		public void waitCompletion(int timeout) throws InterruptedException {
			checkState(countDownLatch!=null);
			countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
		}
		
		public void clear() {
			requestData.clear();
		}
	}

	
	private HttpClient client = HttpClientBuilder.create().build();
	
	@Override
	protected RoutesBuilder[] createRouteBuilders() throws Exception {
		AggregatorGroupRouteBuilder aggregatorGroupRouteBuilder = new AggregatorGroupRouteBuilder(hander);
		aggregatorGroupRouteBuilder.setPeriod(500);
		
		return new RoutesBuilder[] {
				new RestRouteBuilder(),
				aggregatorGroupRouteBuilder
		};
	}
	
	private HttpResponse requestWithEventId(String id) {
		try {
			HttpGet httpGet = new HttpGet("http://localhost:8080/events/" + id);
			return client.execute(httpGet);
		} catch( Exception e ) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testGroupedExchange() throws Exception {
		// given request event id
		List<String> eventIds = Arrays.asList("123", "123", "456", "789");
		hander.expectNum(3);
		
		// when
		List<HttpResponse> responses = eventIds.parallelStream().map(this::requestWithEventId).collect(Collectors.toList());
		
		// then
		responses.forEach(response->{
			assertEquals(200, response.getStatusLine().getStatusCode());
		});
		
		hander.waitCompletion(1000);
		
		Map<String, List<Exchange>> requestData = hander.getRequestData();
		assertEquals(3, requestData.size());
		assertEquals(2, requestData.get("123").size());
		assertEquals(1, requestData.get("456").size());
		assertEquals(1, requestData.get("789").size());
	}

	private void requestTwiceWithId(String id) throws Exception {
		// request event {id} twice
		List<String> eventIds = Arrays.asList(id, id);
		hander.expectNum(1);
		List<HttpResponse> responses = eventIds.parallelStream().map(this::requestWithEventId).collect(Collectors.toList());
		responses.forEach(response->{
			assertEquals(200, response.getStatusLine().getStatusCode());
		});
		
		hander.waitCompletion(1000);
		
		// confirm request result
		Map<String, List<Exchange>> requestData = hander.getRequestData();
		assertEquals(1, requestData.size());
		assertEquals(2, requestData.get("123").size());
		
		// clear first request data
		hander.clear();
		assertTrue(hander.getRequestData().isEmpty());
	}
	
	@Test
	public void testCompletionInterval() throws Exception {
		requestTwiceWithId("123");
		requestTwiceWithId("123");
	}
}

