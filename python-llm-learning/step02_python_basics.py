# Step 2: the Python you need for talking to an LLM.
# Variables, f-strings, functions, lists and dictionaries.

# Variables don't need a type. Python works it out.
name = "Anil"
year = 2026

# An f-string puts values inside text with {braces}.
print(f"Hello, {name}! It is {year}.")


# A function takes inputs and returns a result.
def greet(person: str) -> str:
    return f"Nice to meet you, {person}."


print(greet("Claude"))

# A list is an ordered collection, like a Java List.
questions = ["What is 2 + 2?", "What is the capital of France?"]
for question in questions:
    print(f"Question: {question}")

# A dictionary maps keys to values, like a Java Map.
# This exact shape is how you send one message to Claude.
message = {"role": "user", "content": "What is 2 + 2?"}
print(f"The {message['role']} asks: {message['content']}")

# A conversation is just a list of these dictionaries.
conversation = [
    {"role": "user", "content": "Hi!"},
    {"role": "assistant", "content": "Hello! How can I help?"},
    {"role": "user", "content": "What is 2 + 2?"},
]
print(f"The conversation has {len(conversation)} messages.")
