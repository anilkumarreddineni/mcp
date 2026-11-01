package com.pge.chatapp.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

	private final ChatClient chatClient;

	public ChatService(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	public String reply(String conversationId, String message) {
		return this.chatClient.prompt()
			.user(message)
			.advisors((advisor) -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
			.call()
			.content();
	}

}
