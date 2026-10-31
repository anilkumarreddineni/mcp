package com.pge.mcpserver.clients;

public class OrderServiceUnavailableException extends RuntimeException {

	public OrderServiceUnavailableException(String baseUrl) {
		super("Order service is unavailable at " + baseUrl + ". Please try again later.");
	}

}
