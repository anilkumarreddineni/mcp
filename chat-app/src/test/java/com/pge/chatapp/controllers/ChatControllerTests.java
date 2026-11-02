package com.pge.chatapp.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pge.chatapp.services.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatController.class)
class ChatControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ChatService chatService;

	@Test
	void chatReturnsReplyForSessionConversation() throws Exception {
		MockHttpSession session = new MockHttpSession();
		given(this.chatService.reply(session.getId(), "What's the status of order 1003?"))
			.willReturn("Order 1003 has shipped.");

		this.mockMvc
			.perform(post("/api/chat").session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"message":"What's the status of order 1003?"}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.reply").value("Order 1003 has shipped."));
	}

	@Test
	void chatRejectsBlankMessage() throws Exception {
		this.mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("""
				{"message":"   "}
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.reply").value("Please type a message."));
		verifyNoInteractions(this.chatService);
	}

	@Test
	void chatReturnsFriendlyErrorWhenBackendUnavailable() throws Exception {
		given(this.chatService.reply(anyString(), eq("Show me all orders")))
			.willThrow(new IllegalStateException("Connection refused"));

		this.mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("""
				{"message":"Show me all orders"}
				"""))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.reply").value(ChatController.UNAVAILABLE_REPLY));
	}

}
