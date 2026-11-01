import logging
import time

import anthropic

log = logging.getLogger(__name__)

DEFAULT_MODEL = "claude-haiku-5-5"

SYSTEM_PROMPT = (
    "You are a helpful assistant in a command-line chat. "
    "Answer in short plain text without Markdown, because the terminal shows it as is."
)


class ChatSession:
    """Keeps one conversation with Claude, so follow-up questions have context."""

    def __init__(self, client: anthropic.Anthropic, model: str = DEFAULT_MODEL):
        self.client = client
        self.model = model
        self.messages = []

    def send(self, text: str) -> str:
        self.messages.append({"role": "user", "content": text})
        start = time.monotonic()
        try:
            response = self.client.messages.create(
                model=self.model,
                max_tokens=16000,
                system=SYSTEM_PROMPT,
                messages=self.messages,
            )
        except anthropic.APIError:
            # Drop the unanswered question so the history stays valid for the next try
            self.messages.pop()
            raise

        elapsed_ms = int((time.monotonic() - start) * 1000)
        # Prompts and the API key are never logged, only call metadata
        log.info(
            "model=%s stop_reason=%s input_tokens=%s output_tokens=%s duration_ms=%s request_id=%s",
            response.model,
            response.stop_reason,
            response.usage.input_tokens,
            response.usage.output_tokens,
            elapsed_ms,
            response._request_id,
        )

        # Keep the full content, including thinking blocks, so the history stays append-only
        self.messages.append({"role": "assistant", "content": response.content})

        if response.stop_reason == "refusal":
            return "Sorry, I can't help with that request."
        return "".join(block.text for block in response.content if block.type == "text")
