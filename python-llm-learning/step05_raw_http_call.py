# Step 5: your first real call to an LLM, with plain HTTP.
# This uses only Python's standard library, so you can see exactly what goes over the wire:
# one POST request with three headers and a JSON body.

import json
import os
import sys
import urllib.error
import urllib.request

api_key = os.environ.get("ANTHROPIC_API_KEY")
if not api_key:
    sys.exit("ANTHROPIC_API_KEY is not set. See step 3.")

model = os.environ.get("ANTHROPIC_MODEL") or "claude-opus-5-5"

body = {
    "model": model,
    "max_tokens": 16000,
    "messages": [{"role": "user", "content": "In one sentence, what is a large language model?"}],
}

request = urllib.request.Request(
    "https://api.anthropic.com/v1/messages",
    data=json.dumps(body).encode("utf-8"),
    method="POST",
    headers={
        "content-type": "application/json",
        "x-api-key": api_key,  # who you are
        "anthropic-version": "2026-10-21",  # which version of the API you expect
    },
)

try:
    with urllib.request.urlopen(request, timeout=120) as response:
        reply = json.loads(response.read())
except urllib.error.HTTPError as error:
    # The API explains what went wrong in a JSON error body
    sys.exit(f"HTTP {error.code}: {error.read().decode('utf-8')}")

# Same parsing as step 4, now on a real reply
answer = "".join(block["text"] for block in reply["content"] if block["type"] == "text")
print(f"Claude ({reply['model']}): {answer}")
print(f"stop_reason={reply['stop_reason']} "
      f"input_tokens={reply['usage']['input_tokens']} output_tokens={reply['usage']['output_tokens']}")
