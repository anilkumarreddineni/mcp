package com.pge.mcpserver.tools;

public class OrderNotFoundException extends RuntimeException {

	public OrderNotFoundException(String orderId) {
		super("Order " + orderId + " not found");
	}

}
