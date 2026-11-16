package com.pge.mcpserver.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import com.pge.mcpserver.clients.OrderServiceClient;
import com.pge.mcpserver.clients.OrderServiceUnavailableException;
import com.pge.mcpserver.model.OrderStatus;
import com.pge.mcpserver.model.OrderStatusResponse;
import org.junit.jupiter.api.Test;

class OrderToolsTests {

	private final OrderServiceClient client = mock(OrderServiceClient.class);

	private final OrderTools tools = new OrderTools(this.client);

	@Test
	void listOrdersReturnsOrdersFromOrderService() {
		List<OrderStatusResponse> orders = List.of(new OrderStatusResponse("1001", OrderStatus.PENDING),
				new OrderStatusResponse("1004", OrderStatus.DELIVERED));
		given(this.client.findAll()).willReturn(orders);

		assertThat(this.tools.listOrders()).isEqualTo(orders);
	}

	@Test
	void getOrderStatusReturnsOrderWhenFound() {
		OrderStatusResponse order = new OrderStatusResponse("1003", OrderStatus.SHIPPED);
		given(this.client.findStatus("1003")).willReturn(Optional.of(order));

		assertThat(this.tools.getOrderStatus("1003")).isEqualTo(order);
	}

	@Test
	void getOrderStatusThrowsNotFoundForUnknownOrder() {
		given(this.client.findStatus("9999")).willReturn(Optional.empty());

		assertThatThrownBy(() -> this.tools.getOrderStatus("9999")).isInstanceOf(OrderNotFoundException.class)
			.hasMessage("Order 9999 not found");
	}

	@Test
	void listOrdersPropagatesOrderServiceUnavailable() {
		given(this.client.findAll())
			.willThrow(new OrderServiceUnavailableException("http://localhost:8081"));

		assertThatThrownBy(this.tools::listOrders).isInstanceOf(OrderServiceUnavailableException.class);
	}

}
