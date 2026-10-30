package com.pge.mcpserver.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pge.mcpserver.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@Import(OrderService.class)
class OrderControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getStatusReturnsStatusForKnownOrder() throws Exception {
		this.mockMvc.perform(get("/orders/1003/status"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.orderId").value("1003"))
			.andExpect(jsonPath("$.status").value("SHIPPED"));
	}

	@Test
	void getStatusReturnsNotFoundForUnknownOrder() throws Exception {
		this.mockMvc.perform(get("/orders/9999/status"))
			.andExpect(status().isNotFound());
	}

}
