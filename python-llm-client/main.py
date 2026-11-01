import logging
import os
import sys
from pathlib import Path

import anthropic

from llm_client.chat import DEFAULT_MODEL, ChatSession

LOG_FILE = Path(__file__).parent / "logs" / "llm-client.log"

EXIT_COMMANDS = {"quit", "exit"}


def configure_logging() -> None:
    # Log to a file so the chat in the terminal stays readable
    LOG_FILE.parent.mkdir(exist_ok=True)
    logging.basicConfig(
        filename=LOG_FILE,
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s: %(message)s",
    )


def error_message(error: anthropic.APIError) -> str:
    if isinstance(error, anthropic.AuthenticationError):
        return "Claude rejected the API key. Check ANTHROPIC_API_KEY."
    if isinstance(error, anthropic.NotFoundError):
        return "Claude doesn't know that model. Check ANTHROPIC_MODEL."
    if isinstance(error, anthropic.RateLimitError):
        return "Too many requests to Claude. Wait a moment and try again."
    if isinstance(error, anthropic.APIConnectionError):
        return "Can't reach the Claude API. Check your internet connection and try again."
    if isinstance(error, anthropic.APIStatusError) and error.status_code >= 500:
        return "The Claude API is having problems. Try again in a moment."
    return "Claude couldn't answer that request. See logs/llm-client.log for details."


def run(session: ChatSession, read=input, write=print) -> None:
    write(f"Chatting with {session.model}. Type 'quit' to exit.")
    while True:
        try:
            text = read("You: ").strip()
        except (EOFError, KeyboardInterrupt):
            write("")
            return
        if not text:
            continue
        if text.lower() in EXIT_COMMANDS:
            return
        try:
            write(f"Claude: {session.send(text)}")
        except anthropic.APIError as error:
            logging.getLogger(__name__).warning("Claude API call failed", exc_info=error)
            write(error_message(error))


def main() -> int:
    if not os.environ.get("ANTHROPIC_API_KEY"):
        print("ANTHROPIC_API_KEY is not set. Set it to your Anthropic API key and run again.", file=sys.stderr)
        return 1
    configure_logging()
    client = anthropic.Anthropic()
    run(ChatSession(client, os.environ.get("ANTHROPIC_MODEL") or DEFAULT_MODEL))
    return 0


if __name__ == "__main__":
    sys.exit(main())
