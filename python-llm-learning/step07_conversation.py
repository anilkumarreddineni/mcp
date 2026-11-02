# Step 7: a conversation. Claude has no memory between calls,
# so you send the whole history every time and add each reply to it.

import os

import anthropic

model = os.environ.get("ANTHROPIC_MODEL") or "claude-opus-5-5"
client = anthropic.Anthropic()

# A system prompt sets how Claude behaves for the whole conversation
SYSTEM_PROMPT = "You are a friendly tutor. Answer in at most three short sentences of plain text."

history = []
print(f"Chatting with {model}. Type 'quit' to exit.")

while True:
    try:
        question = input("You: ").strip()
    except (EOFError, KeyboardInterrupt):
        break
    if question.lower() in {"quit", "exit"}:
        break
    if not question:
        continue

    history.append({"role": "user", "content": question})
    try:
        response = client.messages.create(
            model=model,
            max_tokens=16000,
            system=SYSTEM_PROMPT,
            messages=history,
        )
    except anthropic.APIError as error:
        history.pop()  # forget the unanswered question so the history stays valid
        print(f"The call failed: {error}")
        continue

    # Store the full reply, not just the text, so the next call sees exactly what Claude said
    history.append({"role": "assistant", "content": response.content})

    if response.stop_reason == "refusal":
        print("Claude: Sorry, I can't help with that.")
    else:
        print("Claude:", "".join(block.text for block in response.content if block.type == "text"))

print(f"Bye! The conversation had {len(history)} messages.")
