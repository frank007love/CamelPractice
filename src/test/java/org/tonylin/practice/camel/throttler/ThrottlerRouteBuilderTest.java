package org.tonylin.practice.camel.throttler;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.tonylin.practice.camel.rest.RestHandler;
import org.tonylin.practice.camel.rest.RestRouteBuilder;

public class ThrottlerRouteBuilderTest extends CamelTestSupport {

	private RestHandler hander = new RestHandler();
	private static final int limit = 2;
	private static final int period = 200;
	
	private HttpClient client = HttpClientBuilder.create().build();
	private HttpGet httpGet = new HttpGet("http://localhost:8080/events/123");
	
	@Override
	protected RoutesBuilder[] createRouteBuilders() throws Exception {
		ThrottlerRouteBuilder throttlerRouteBuilder = new ThrottlerRouteBuilder(hander);
		throttlerRouteBuilder.setLimit(limit);
		throttlerRouteBuilder.setPeriod(period);
		
		return new RoutesBuilder[] {
				new RestRouteBuilder(),
				throttlerRouteBuilder
		};
	}
	
	private List<HttpResponse> batchRequest(int times) throws Exception {
		List<HttpResponse> responses = new ArrayList<HttpResponse>();
		for( int i = 0 ; i < times ; i++ ) {
			responses.add(client.execute(httpGet));
		}
		return responses;
	}
	
	@Test
	public void testOverload() throws Exception {
		// when request 3 times
		List<HttpResponse> responses = batchRequest(3);

		// then
		assertEquals(2, hander.getRequestIds().size());
		assertEquals(200, responses.get(0).getStatusLine().getStatusCode());
		assertEquals(200, responses.get(1).getStatusLine().getStatusCode());
		assertEquals(503, responses.get(2).getStatusLine().getStatusCode());
	}

	@Test
	public void testThrottlePeriod() throws Exception  {
		// when
		List<HttpResponse> responses = batchRequest(3);
		Thread.sleep(period+1);
		
		responses.addAll(batchRequest(3));
		
		// then
		assertEquals(4, hander.getRequestIds().size());
		assertEquals(200, responses.get(0).getStatusLine().getStatusCode());
		assertEquals(200, responses.get(1).getStatusLine().getStatusCode());
		assertEquals(503, responses.get(2).getStatusLine().getStatusCode());
		assertEquals(200, responses.get(3).getStatusLine().getStatusCode());
		assertEquals(200, responses.get(4).getStatusLine().getStatusCode());
		assertEquals(503, responses.get(5).getStatusLine().getStatusCode());
	}
}
