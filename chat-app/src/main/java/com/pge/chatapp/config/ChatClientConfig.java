package com.pge.chatapp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	private static final String SYSTEM_PROMPT = """
			You are an order assistant. You help users look up orders and their status.
			Always use the available tools to answer questions about orders, and never guess an
			order's status. If a tool reports that an order was not found, tell the user that
			order does not exist. If a question is not about orders, politely say that you can
			only help with order questions. Keep answers short and clear, and write plain text
			without Markdown, because the chat window shows text exactly as written.
			The conversation history shows only the text of earlier replies, not the tool calls
			behind them. Your earlier replies about orders were already based on tool results,
			so do not apologize for, correct or re-explain them. Answer only the latest question.
			""";

	@Bean
	ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory, ToolCallbackProvider mcpTools) {
		// Passing the provider (not its tools) defers the call to mcp-server until a chat
		// request, so chat-app starts even when mcp-server is down
		// Advisors run outermost first: chat memory, then the tool-calling loop, then the logger.
		// The logger sits inside the loop, so it logs every Claude API call, including
		// the requests that return tool results to Claude
		return builder.defaultSystem(SYSTEM_PROMPT)
			.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
					ToolCallingAdvisor.builder().build(), SimpleLoggerAdvisor.builder().build())
			.defaultTools(mcpTools)
			.build();
	}

}
