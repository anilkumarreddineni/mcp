# Step 4: JSON, the format LLM APIs speak.
# json.dumps turns a dictionary into text; json.loads turns text back into a dictionary.
# No network yet: this step only builds a request and reads a sample response.

import json

# The request body we will send to Claude in step 5
request = {
    "model": "claude-haiku-5-5",
    "max_tokens": 16000,
    "messages": [{"role": "user", "content": "What is 2 + 2?"}],
}
print("Request as JSON text:")
print(json.dumps(request, indent=2))

# What a reply from Claude looks like (shortened)
sample_reply = """
{
  "id": "msg_123",
  "model": "claude-haiku-5-5",
  "role": "assistant",
  "content": [
    {"type": "text", "text": "2 + 2 equals 4."}
  ],
  "stop_reason": "end_turn",
  "usage": {"input_tokens": 14, "output_tokens": 10}
}
"""
reply = json.loads(sample_reply)

# "content" is a list of blocks. Keep only the text blocks.
answer = "".join(block["text"] for block in reply["content"] if block["type"] == "text")
print(f"\nClaude's answer: {answer}")
print(f"Tokens used: {reply['usage']['input_tokens']} in, {reply['usage']['output_tokens']} out")
