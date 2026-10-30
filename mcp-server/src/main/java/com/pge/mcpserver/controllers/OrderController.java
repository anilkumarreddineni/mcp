package com.pge.mcpserver.controllers;

import com.pge.mcpserver.model.OrderStatusResponse;
import com.pge.mcpserver.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/orders/{orderId}/status")
	public ResponseEntity<OrderStatusResponse> getStatus(@PathVariable String orderId) {
		return this.orderService.findStatus(orderId)
			.map((status) -> ResponseEntity.ok(new OrderStatusResponse(orderId, status)))
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

}
