# python-llm-learning

Learn to call an LLM from Python, one small step at a time. Each step is a single file that adds one new idea. Run them in order and read the code as you go.

| Step | File | You learn |
|------|------|-----------|
| 1 | `step01_hello_world.py` | Running a Python file |
| 2 | `step02_python_basics.py` | Variables, f-strings, functions, lists and dictionaries, and the shape of a chat message |
| 3 | `step03_environment_variables.py` | Reading the API key from the environment instead of the code |
| 4 | `step04_json.py` | Building a request and reading a reply as JSON, offline |
| 5 | `step05_raw_http_call.py` | **First LLM call**, with plain HTTP and no libraries |
| 6 | `step06_first_sdk_call.py` | The same call with the official `anthropic` SDK |
| 7 | `step07_conversation.py` | A chat that remembers earlier messages |

Steps 1 to 4 are free and don't need a key. Steps 5 to 7 call Claude and cost a fraction of a cent per question.

## Setup

Needs Python 3.10 or later. From this directory, in PowerShell:

```powershell
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
$env:ANTHROPIC_API_KEY = "sk-ant-..."   # from https://platform.claude.com, never commit it
```

Steps 1 to 5 only use the standard library, so they work before `pip install`.

## Run

```powershell
python step01_hello_world.py
python step02_python_basics.py
# ...and so on up to
python step07_conversation.py
```

Steps 5 to 7 use `claude-opus-5-5`. To try another model, set `ANTHROPIC_MODEL`, for example `$env:ANTHROPIC_MODEL = "claude-haiku-5-5"` for a cheaper one.

## Where to go next

`../python-llm-client` is the finished version of step 7: the same chat, with logging, friendlier error messages and tests.
