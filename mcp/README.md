## How to manage MUNSIKSA service using Gemini CLI

### 1. Install Gemini CLI

```bash
npm install -g @google/gemini-cli
```

### 2. Create `.gemini/settings.json` in the root directory

```json
{
  "mcpServers": {
    "munsiksa-admin": {
      "command": "node",
      "args": ["YOUR_DIRECTORY\\shopping-mall-back\\mcp\\build\\index.js"]
    }
  }
}
```

### 3. Run Gemini CLI at the root directory

```bash
gemini
```

- If there is no MCP server connected, check follow:
  - Did you create `.env` file in the root directory?
  - Did you run Spring Boot server?

### 4. Ask Gemini

> "Give 1000 welcome points to 10 members who recently joined."

> "Show me a list of members with more than 50000 points."