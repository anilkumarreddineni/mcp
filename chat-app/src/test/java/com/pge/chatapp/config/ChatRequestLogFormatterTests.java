package com.pge.chatapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

class ChatRequestLogFormatterTests {

	@Test
	void formatShowsEachMessageWithItsRoleToolCallsAndToolResults() {
		AssistantMessage toolCall = AssistantMessage.builder()
			.content("")
			.toolCalls(List.of(new AssistantMessage.ToolCall("toolu_1", "function", "getOrderStatus",
					"{\"orderId\":\"1003\"}")))
			.build();
		ToolResponseMessage toolResult = ToolResponseMessage.builder()
			.responses(List.of(new ToolResponseMessage.ToolResponse("toolu_1", "getOrderStatus",
					"{\"status\":\"SHIPPED\"}")))
			.build();
		Prompt prompt = new Prompt(
				List.of(new SystemMessage("You are an order assistant.\nKeep answers short."),
						new UserMessage("status of 1003?"), toolCall, toolResult),
				ChatOptions.builder().model("claude-haiku-5-5").build());
		ChatClientRequest request = new ChatClientRequest(prompt, Map.of(ChatMemory.CONVERSATION_ID, "abc"));

		assertThat(ChatRequestLogFormatter.format(request)).isEqualTo("""
				Claude API request (model claude-haiku-5-5) conversation abc, 4 messages:
				  [1] SYSTEM:
				    You are an order assistant.
				    Keep answers short.
				  [2] USER:
				    status of 1003?
				  [3] ASSISTANT:
				    calls getOrderStatus {"orderId":"1003"}
				  [4] TOOL:
				    result of getOrderStatus: {"status":"SHIPPED"}""");
	}

}
