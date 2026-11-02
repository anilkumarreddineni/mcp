# Step 3: read the API key from an environment variable.
# Never write a key into your code: it would end up in git.

import os
import sys

api_key = os.environ.get("ANTHROPIC_API_KEY")

if not api_key:
    print("ANTHROPIC_API_KEY is not set.")
    print('PowerShell:  $env:ANTHROPIC_API_KEY = "sk-ant-..."')
    sys.exit(1)

# Show only that the key exists, never the key itself.
print(f"Found ANTHROPIC_API_KEY ({len(api_key)} characters).")
