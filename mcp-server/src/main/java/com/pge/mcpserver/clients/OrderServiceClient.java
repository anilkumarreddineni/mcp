package com.pge.mcpserver.clients;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.pge.mcpserver.model.OrderStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class OrderServiceClient {

	private final RestClient restClient;

	private final String baseUrl;

	public OrderServiceClient(RestClient.Builder restClientBuilder,
			@Value("${order-service.base-url}") String baseUrl) {
		this.restClient = restClientBuilder.baseUrl(baseUrl).build();
		this.baseUrl = baseUrl;
	}

	public List<OrderStatusResponse> findAll() {
		return call(() -> this.restClient.get()
			.uri("/orders")
			.retrieve()
			.body(new ParameterizedTypeReference<List<OrderStatusResponse>>() {
			}));
	}

	public Optional<OrderStatusResponse> findStatus(String orderId) {
		try {
			return Optional.ofNullable(call(() -> this.restClient.get()
				.uri("/orders/{orderId}/status", orderId)
				.retrieve()
				.body(OrderStatusResponse.class)));
		}
		catch (HttpClientErrorException.NotFound ex) {
			return Optional.empty();
		}
	}

	private <T> T call(Supplier<T> request) {
		try {
			return request.get();
		}
		catch (ResourceAccessException ex) {
			// Log the cause here: MCP tool errors show the root cause's message, which is
			// often null for connection failures
			log.warn("Order service call to {} failed", this.baseUrl, ex);
			throw new OrderServiceUnavailableException(this.baseUrl);
		}
	}

}
