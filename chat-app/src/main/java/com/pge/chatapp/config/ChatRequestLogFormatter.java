package com.pge.chatapp.config;

import java.util.List;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * Turns a chat request into readable log text: one block per message, labelled with its
 * role, with tool calls and tool results shown by name. Replaces the default one-line
 * {@code toString()} dump of {@code SimpleLoggerAdvisor}.
 */
final class ChatRequestLogFormatter {

	private static final String INDENT = "    ";

	private ChatRequestLogFormatter() {
	}

	static String format(ChatClientRequest request) {
		StringBuilder out = new StringBuilder("Claude API request");
		ChatOptions options = request.prompt().getOptions();
		if (options != null && options.getModel() != null) {
			out.append(" (model ").append(options.getModel()).append(')');
		}
		Object conversationId = request.context().get(ChatMemory.CONVERSATION_ID);
		if (conversationId != null) {
			out.append(" conversation ").append(conversationId);
		}
		List<Message> messages = request.prompt().getInstructions();
		out.append(", ").append(messages.size()).append(" messages:");
		for (int i = 0; i < messages.size(); i++) {
			appendMessage(out, i + 1, messages.get(i));
		}
		if (options instanceof ToolCallingChatOptions toolOptions) {
			appendTools(out, toolOptions.getToolCallbacks());
		}
		return out.toString();
	}

	// The tools Claude may call: sent in the request's "tools" field, beside the messages
	private static void appendTools(StringBuilder out, List<ToolCallback> tools) {
		out.append('\n').append("  tools (").append(tools.size()).append("):");
		for (ToolCallback tool : tools) {
			ToolDefinition definition = tool.getToolDefinition();
			appendBlock(out, definition.name() + ": " + definition.description());
			// JSON strings cannot hold raw line breaks, so joining lines keeps the schema valid
			appendBlock(out, "  input: " + definition.inputSchema().replaceAll("\\s*\\R\\s*", ""));
		}
	}

	private static void appendMessage(StringBuilder out, int number, Message message) {
		out.append('\n').append("  [").append(number).append("] ").append(message.getMessageType()).append(':');
		if (message instanceof ToolResponseMessage toolResponse) {
			for (ToolResponseMessage.ToolResponse response : toolResponse.getResponses()) {
				appendBlock(out, "result of " + response.name() + ": " + response.responseData());
			}
			return;
		}
		if (message.getText() != null && !message.getText().isBlank()) {
			appendBlock(out, message.getText().strip());
		}
		if (message instanceof AssistantMessage assistant) {
			for (AssistantMessage.ToolCall call : assistant.getToolCalls()) {
				appendBlock(out, "calls " + call.name() + " " + call.arguments());
			}
		}
	}

	// Indents every line, so multi-line text such as the system prompt stays inside its message
	private static void appendBlock(StringBuilder out, String text) {
		for (String line : text.split("\\R")) {
			out.append('\n').append(INDENT).append(line.stripTrailing());
		}
	}

}
