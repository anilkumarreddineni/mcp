from types import SimpleNamespace
from unittest.mock import MagicMock

import anthropic
import httpx2
import pytest

from llm_client.chat import ChatSession


def claude_response(text, stop_reason="end_turn"):
    return SimpleNamespace(
        model="claude-haiku-5-5",
        stop_reason=stop_reason,
        content=[SimpleNamespace(type="thinking", thinking=""), SimpleNamespace(type="text", text=text)],
        usage=SimpleNamespace(input_tokens=12, output_tokens=5),
        _request_id="req_test",
    )


def connection_error():
    return anthropic.APIConnectionError(request=httpx2.Request("POST", "https://api.anthropic.com/v1/messages"))


def test_send_returns_text_and_keeps_full_history():
    client = MagicMock()
    first, second = claude_response("Paris."), claude_response("About 2 million.")
    client.messages.create.side_effect = [first, second]
    session = ChatSession(client)

    assert session.send("Capital of France?") == "Paris."
    assert session.send("How many people live there?") == "About 2 million."

    messages = client.messages.create.call_args.kwargs["messages"]
    assert messages == [
        {"role": "user", "content": "Capital of France?"},
        {"role": "assistant", "content": first.content},
        {"role": "user", "content": "How many people live there?"},
        {"role": "assistant", "content": second.content},
    ]
    assert client.messages.create.call_args.kwargs["model"] == "claude-haiku-5-5"


def test_send_drops_question_when_call_fails():
    client = MagicMock()
    client.messages.create.side_effect = [connection_error(), claude_response("Hi!")]
    session = ChatSession(client)

    with pytest.raises(anthropic.APIConnectionError):
        session.send("Hello?")
    assert session.messages == []

    assert session.send("Hello again") == "Hi!"
    assert session.messages[0] == {"role": "user", "content": "Hello again"}


def test_send_handles_refusal():
    client = MagicMock()
    client.messages.create.return_value = claude_response("", stop_reason="refusal")

    assert ChatSession(client).send("Something not allowed") == "Sorry, I can't help with that request."


def test_send_uses_configured_model():
    client = MagicMock()
    client.messages.create.return_value = claude_response("Hi!")

    ChatSession(client, model="claude-sonnet-5-5").send("Hello")

    assert client.messages.create.call_args.kwargs["model"] == "claude-sonnet-5-5"
