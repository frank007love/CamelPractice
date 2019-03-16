package org.tonylin.practice.camel.rest;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;


public class EventRouteBuilderTest extends CamelTestSupport {
	
	private RestHandler hander = new RestHandler();
	
	@Override
	protected RoutesBuilder[] createRouteBuilders() throws Exception {
		return new RoutesBuilder[] {
				new RestRouteBuilder(),
				new EventRouteBuilder(hander)
		};
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
