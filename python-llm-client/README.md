# python-llm-client

A command-line chat with Claude, built on the official [`anthropic`](https://pypi.org/project/anthropic/) Python SDK. It keeps the conversation history, so follow-up questions work.

## Setup

Needs Python 3.10 or later. From this directory:

```sh
python -m venv .venv
.venv/Scripts/activate          # macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
```

## Run

```sh
export ANTHROPIC_API_KEY=sk-ant-...   # PowerShell: $env:ANTHROPIC_API_KEY="sk-ant-..."
python main.py
```

Type a question and press Enter. Type `quit` or `exit`, or press Ctrl+D (Ctrl+Z then Enter on Windows), to leave.

| Environment variable | Default            | Purpose             |
|----------------------|--------------------|---------------------|
| `ANTHROPIC_API_KEY`  | none (required)    | Claude API key      |
| `ANTHROPIC_MODEL`    | `claude-haiku-5-5` | Claude model to use |

Errors such as a wrong key, an unknown model or no network connection show a short message, and the chat keeps going.

## Logs

Each API call is logged to `logs/llm-client.log`, which is git-ignored:

- the endpoint and HTTP status, e.g. `POST https://api.anthropic.com/v1/messages "HTTP/1.1 200 OK"`
- the model, stop reason, input and output tokens, duration and request ID

Prompts, replies and the API key are not logged.

## Tests

```sh
pip install -r requirements-dev.txt
pytest
```

The tests mock the Claude client, so they don't need an API key.
