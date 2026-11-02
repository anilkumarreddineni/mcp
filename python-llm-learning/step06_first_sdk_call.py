# Step 6: the same call as step 5, using the official Anthropic SDK.
# The SDK builds the headers and JSON for you, retries on temporary errors,
# and gives you Python objects instead of dictionaries.

import os

import anthropic

model = os.environ.get("ANTHROPIC_MODEL") or "claude-opus-5-5"

# Reads ANTHROPIC_API_KEY from the environment by itself
client = anthropic.Anthropic()

try:
    response = client.messages.create(
        model=model,
        max_tokens=16000,
        messages=[{"role": "user", "content": "In one sentence, what is a large language model?"}],
    )
except anthropic.AuthenticationError:
    raise SystemExit("Claude rejected the API key. Check ANTHROPIC_API_KEY.")
except anthropic.APIError as error:
    raise SystemExit(f"The call failed: {error}")

# Fields are attributes now: response.content, block.text, response.usage.input_tokens
answer = "".join(block.text for block in response.content if block.type == "text")
print(f"Claude ({response.model}): {answer}")
print(f"stop_reason={response.stop_reason} "
      f"input_tokens={response.usage.input_tokens} output_tokens={response.usage.output_tokens}")
