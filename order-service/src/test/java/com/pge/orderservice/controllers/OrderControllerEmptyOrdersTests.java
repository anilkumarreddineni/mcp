package com.pge.orderservice.controllers;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.TreeMap;

import com.pge.orderservice.services.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerEmptyOrdersTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OrderService orderService;

	@Test
	void getAllReturnsEmptyArrayWhenNoOrders() throws Exception {
		given(this.orderService.findAll()).willReturn(new TreeMap<>());
		this.mockMvc.perform(get("/orders"))
			.andExpect(status().isOk())
			.andExpect(content().json("[]"));
	}

}
