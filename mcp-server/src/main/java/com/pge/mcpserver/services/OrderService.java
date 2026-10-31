package com.pge.mcpserver.services;

import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import com.pge.mcpserver.model.OrderStatus;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

	// In-memory sample data until a real order store exists
	private final Map<String, OrderStatus> orders = Map.of(
			"1001", OrderStatus.PENDING,
			"1002", OrderStatus.PROCESSING,
			"1003", OrderStatus.SHIPPED,
			"1004", OrderStatus.DELIVERED,
			"1005", OrderStatus.CANCELLED);

	public Optional<OrderStatus> findStatus(String orderId) {
		return Optional.ofNullable(this.orders.get(orderId));
	}

	public SortedMap<String, OrderStatus> findAll() {
		return new TreeMap<>(this.orders);
	}

}
