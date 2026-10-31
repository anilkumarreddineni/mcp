package com.pge.orderservice.controllers;

import java.util.List;

import com.pge.orderservice.model.OrderStatusResponse;
import com.pge.orderservice.services.OrderService;
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

	@GetMapping("/orders")
	public List<OrderStatusResponse> getAll() {
		return this.orderService.findAll()
			.entrySet()
			.stream()
			.map((entry) -> new OrderStatusResponse(entry.getKey(), entry.getValue()))
			.toList();
	}

	@GetMapping("/orders/{orderId}/status")
	public ResponseEntity<OrderStatusResponse> getStatus(@PathVariable String orderId) {
		return this.orderService.findStatus(orderId)
			.map((status) -> ResponseEntity.ok(new OrderStatusResponse(orderId, status)))
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

}
