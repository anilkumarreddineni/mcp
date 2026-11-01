package com.pge.chatapp.controllers;

import com.pge.chatapp.model.ChatRequest;
import com.pge.chatapp.model.ChatResponse;
import com.pge.chatapp.services.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

	static final String UNAVAILABLE_REPLY = "Sorry, I can't reach the order system or the AI service right now. "
			+ "Please try again in a moment.";

	private final ChatService chatService;

	@PostMapping("/chat")
	public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request, HttpSession session) {
		if (request.message() == null || request.message().isBlank()) {
			return ResponseEntity.badRequest().body(new ChatResponse("Please type a message."));
		}
		// One conversation per browser session, so follow-up questions keep their context
		return ResponseEntity.ok(new ChatResponse(this.chatService.reply(session.getId(), request.message())));
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ChatResponse> handleFailure(RuntimeException ex) {
		log.warn("Chat request failed", ex);
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ChatResponse(UNAVAILABLE_REPLY));
	}

}
