package com.pge.mcpserver.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.net.ConnectException;

import com.pge.mcpserver.model.OrderStatus;
import com.pge.mcpserver.model.OrderStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OrderServiceClientTests {

	private static final String BASE_URL = "http://order-service.test";

	private MockRestServiceServer server;

	private OrderServiceClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		this.server = MockRestServiceServer.bindTo(builder).build();
		this.client = new OrderServiceClient(builder, BASE_URL);
	}

	@Test
	void findAllReturnsAllOrders() {
		this.server.expect(requestTo(BASE_URL + "/orders"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("""
					[{"orderId":"1001","status":"PENDING"},{"orderId":"1003","status":"SHIPPED"}]
					""", MediaType.APPLICATION_JSON));

		assertThat(this.client.findAll()).containsExactly(new OrderStatusResponse("1001", OrderStatus.PENDING),
				new OrderStatusResponse("1003", OrderStatus.SHIPPED));
		this.server.verify();
	}

	@Test
	void findStatusReturnsOrderWhenFound() {
		this.server.expect(requestTo(BASE_URL + "/orders/1002/status"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("""
					{"orderId":"1002","status":"PROCESSING"}
					""", MediaType.APPLICATION_JSON));

		assertThat(this.client.findStatus("1002")).contains(new OrderStatusResponse("1002", OrderStatus.PROCESSING));
		this.server.verify();
	}

	@Test
	void findStatusReturnsEmptyWhenOrderNotFound() {
		this.server.expect(requestTo(BASE_URL + "/orders/9999/status")).andRespond(withResourceNotFound());

		assertThat(this.client.findStatus("9999")).isEmpty();
		this.server.verify();
	}

	@Test
	void findAllThrowsWhenOrderServiceUnreachable() {
		this.server.expect(requestTo(BASE_URL + "/orders"))
			.andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(this.client::findAll).isInstanceOf(OrderServiceUnavailableException.class)
			.hasMessageContaining("Order service is unavailable at " + BASE_URL);
	}

	@Test
	void findStatusThrowsWhenOrderServiceUnreachable() {
		this.server.expect(requestTo(BASE_URL + "/orders/1001/status"))
			.andRespond(withException(new IOException("Read timed out")));

		assertThatThrownBy(() -> this.client.findStatus("1001"))
			.isInstanceOf(OrderServiceUnavailableException.class);
	}

}
