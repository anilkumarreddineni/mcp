package com.pge.mcpserver.tools;

import java.util.List;

import com.pge.mcpserver.clients.OrderServiceClient;
import com.pge.mcpserver.model.OrderStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpTool.McpAnnotations;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderTools {

	private final OrderServiceClient orderServiceClient;

	@McpTool(name = "listOrders",
			description = "List all orders with their current status (PENDING, PROCESSING, SHIPPED, DELIVERED or CANCELLED). "
					+ "Use this to browse orders or when the order ID is not known.",
			annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true))
	public List<OrderStatusResponse> listOrders() {
		return this.orderServiceClient.findAll();
	}

	@McpTool(name = "getOrderStatus",
			description = "Get the current status (PENDING, PROCESSING, SHIPPED, DELIVERED or CANCELLED) of a single order by its ID. "
					+ "Fails with 'not found' if no order has that ID.",
			annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true))
	public OrderStatusResponse getOrderStatus(
			@McpToolParam(description = "The ID of the order to look up, for example 1001") String orderId) {
		return this.orderServiceClient.findStatus(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
	}

}
