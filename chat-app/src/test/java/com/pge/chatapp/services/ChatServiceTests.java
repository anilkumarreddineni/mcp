package com.pge.chatapp.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

class ChatServiceTests {

	private final ChatClient chatClient = mock(ChatClient.class);

	private final ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);

	private final ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

	private final ChatService chatService = new ChatService(this.chatClient);

	@Test
	@SuppressWarnings("unchecked")
	void replySendsMessageWithConversationIdAndReturnsContent() {
		given(this.chatClient.prompt()).willReturn(this.requestSpec);
		given(this.requestSpec.user("and order 1004?")).willReturn(this.requestSpec);
		given(this.requestSpec.advisors(any(Consumer.class))).willReturn(this.requestSpec);
		given(this.requestSpec.call()).willReturn(this.callResponseSpec);
		given(this.callResponseSpec.content()).willReturn("Order 1004 was delivered.");

		assertThat(this.chatService.reply("session-1", "and order 1004?")).isEqualTo("Order 1004 was delivered.");

		ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> advisorCaptor = ArgumentCaptor.forClass(Consumer.class);
		verify(this.requestSpec).advisors(advisorCaptor.capture());
		ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
		advisorCaptor.getValue().accept(advisorSpec);
		verify(advisorSpec).param(ChatMemory.CONVERSATION_ID, "session-1");
	}

}
