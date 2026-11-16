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
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

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
				ToolCallingChatOptions.builder()
					.model("claude-haiku-5-5")
					.toolCallbacks(List.of(tool("getOrderStatus", "Get the status of an order.", """
							{
							  "type" : "object",
							  "properties" : { "orderId" : { "type" : "string" } }
							}""")))
					.build());
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
				    result of getOrderStatus: {"status":"SHIPPED"}
				  tools (1):
				    getOrderStatus: Get the status of an order.
				      input: {"type" : "object","properties" : { "orderId" : { "type" : "string" } }}""");
	}

	private static ToolCallback tool(String name, String description, String inputSchema) {
		ToolDefinition definition = ToolDefinition.builder()
			.name(name)
			.description(description)
			.inputSchema(inputSchema)
			.build();
		return new ToolCallback() {
			@Override
			public ToolDefinition getToolDefinition() {
				return definition;
			}

			@Override
			public String call(String toolInput) {
				return "";
			}
		};
	}

}
