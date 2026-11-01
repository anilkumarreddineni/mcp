from unittest.mock import MagicMock

import anthropic
import httpx2

import main


def scripted(*lines):
    answers = iter(lines)

    def read(prompt):
        try:
            return next(answers)
        except StopIteration:
            raise EOFError

    return read


def status_error(error_class, status_code):
    request = httpx2.Request("POST", "https://api.anthropic.com/v1/messages")
    return error_class("error", response=httpx2.Response(status_code, request=request), body=None)


def test_run_answers_until_quit():
    session = MagicMock(model="claude-haiku-5-5")
    session.send.return_value = "Paris."
    output = []

    main.run(session, read=scripted("Capital of France?", "", "quit", "never asked"), write=output.append)

    session.send.assert_called_once_with("Capital of France?")
    assert output == ["Chatting with claude-haiku-5-5. Type 'quit' to exit.", "Claude: Paris."]


def test_run_shows_friendly_error_and_keeps_going():
    session = MagicMock(model="claude-haiku-5-5")
    session.send.side_effect = [
        anthropic.APIConnectionError(request=httpx2.Request("POST", "https://api.anthropic.com/v1/messages")),
        "Hi!",
    ]
    output = []

    main.run(session, read=scripted("Hello?", "Hello again"), write=output.append)

    assert output[1] == "Can't reach the Claude API. Check your internet connection and try again."
    assert output[2] == "Claude: Hi!"


def test_error_messages():
    assert "ANTHROPIC_API_KEY" in main.error_message(status_error(anthropic.AuthenticationError, 401))
    assert "ANTHROPIC_MODEL" in main.error_message(status_error(anthropic.NotFoundError, 404))
    assert "Too many requests" in main.error_message(status_error(anthropic.RateLimitError, 429))
    assert "having problems" in main.error_message(status_error(anthropic.InternalServerError, 500))


def test_main_without_api_key_prints_help(monkeypatch, capsys):
    monkeypatch.delenv("ANTHROPIC_API_KEY", raising=False)

    assert main.main() == 1
    assert "ANTHROPIC_API_KEY is not set" in capsys.readouterr().err
