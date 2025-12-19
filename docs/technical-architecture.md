## Technical Architecture
![HLA](../docs/assets/HLA-Agent-Framework.png)

### Backend Architecture
The home banking assistant is designed as conversational multi-agent system with each agent specializing in a specific functional domain (e.g., account management, transaction history, payments). The architecture consists of the following key components:

- **Agents App (Microservice)**: Serves as the central hub for processing user chat requests. It's a [FastAPI](https://fastapi.tiangolo.com/) app which uses  **agent-framework** to create Agents equipped with tools and orchestrate them using [hand-off pattern](https://learn.microsoft.com/en-us/azure/architecture/ai-ml/guide/ai-agent-design-patterns#handoff-orchestration).
    - **Supervisor Agent**: It's responsible to triage the user request, and delegate the task to the specialized domain agent. This component ensures that user queries are efficiently handled by the relevant agent. Agents are engaged by the supervisor in a single turn conversation meaning that only one is selected by the supervisor to answer to user task. 
    
    - **Account Agent**: Specializes in handling tasks related to banking account information, credit balance, and registered payment methods. It leverages specific Account service APIs to fetch and manage account-related data. The Microsoft Agent Framework is used to create account specific tools definition from the MCP server and automatically call the HTTP endpoint with input parameters extracted by gpt4 model from the chat conversation.

    - **Transactions Agent**: Focuses on tasks related to querying user bank movements, including income and outcome payments. This agent accesses account mcp server to retrieve accountid and transaction history mcp server to search for transactions and present them to the user.

    - **Payments Agent**: Focuses on managing tasks related to submitting payments. It interacts with multiple MCP servers and tools, such as ScanInvoice (backed by Azure Document Intelligence), Account Service to retrieve account and payment methods info, Payment Service to submit payment processing and Transaction History service to check for previous paid invoices.

- **Existing Business APIs**: Interfaces with the backend systems to perform operations related to personal banking accounts, transactions, and invoice payments. These APIs are implemented as external spring boot microservices providing the necessary data and functionality consumed by agents to execute their tasks. They are exposed as MCP endpoints using [FastMCP](https://gofastmcp.com/getting-started/welcome) to be consumed by agents.
    - **Account MCP Service (Microservice)**: Provides functionalities like retrieving account details by username, fetching payment methods, and getting registered beneficiaries. This microservice supports all 3 agents.

    - **Payments MCP Service (Microservice)**: Offers capabilities to submit payments and notify transactions. It is a critical component for the Payments Agent to execute payment-related tasks efficiently.

    - **Reporting MCP Service (Microservice)**: Enables searching transactions and retrieving transactions by recipient. This service supports the Transactions Agent in providing detailed transaction reports to the user and the Payment Agent as it needs to check if an invoice has not been already paid.

- **Multi agent orchestration**: The supervisor pattern to orchestrate and delegate tasks to different domain specific agents is implemented using different approaches available for you to explore
  - **Hand-off pattern + Chatkit**: This pattern is implemented using the [hand-off orchestration](https://learn.microsoft.com/en-us/azure/architecture/ai-ml/guide/ai-agent-design-patterns#handoff-orchestration) available as pre-built orchestration in the Microsoft Agent Framework. Look here for code details [handoff_orchestrator_chatkit.py](app/backend/app/agents/azure_chat/handoff/chatkit/handoff_orchestrator_chatkit.py). Furthermore the chat ui and  agents interaction is implemented using [OpenAI chatkit protocol](https://platform.openai.com/docs/guides/chatkit) for better human in the loop experience supporting agents progress notification and tool approval widget streaming.
  - **Hand-off pattern + custom UI-Agent protocol**: As the above but without using the chatkit protocol specification. This has limited support for HITL patterns and use a simple chat interface that's not integrated into an existing home banking app.Look here for code details [handoff_orchestrator_custom_protocol.py](app/backend/app/agents/azure_chat/handoff/handoff_orchestrator.py)
  - **Domain agents as tools**: In this approach the domain specific agents are exposed as tools to the supervisor agent which can call them as tools when needed.That is based on Agent Framework Agent abstractions only. Look here for code details [supervisor_agent.py](app/backend/app/agents/azure_chat/agents_as_tools/supervisor_agent.py)

### Frontend Architecture
This project provides two frontend options to choose from, each with different capabilities and backend integration requirements:

#### 1. **banking-web** (Default - Chatkit Protocol)
A fully-featured React Single Page Application built with React shadcn/ui that supports:
- Rich human-in-the-loop (HITL) experience
- Agent progress notifications
- Tool approval widgets
- Image upload support for invoices and receipts
- Chatkit protocol compliance
- Integrated into a banking app interface

**To use this frontend:**
- In `azure.yaml`: Ensure the `banking-web` service is **uncommented** and `simple-chat` is **commented out**
- In `app/backend/Dockerfile`: Use the CMD line:
  ```dockerfile
  CMD ["uvicorn", "app.main_chatkit_server:app", "--host", "0.0.0.0", "--port", "8080"]
  ```

#### 2. **simple-chat** (Custom Protocol)
A lightweight chat interface with basic conversational capabilities:
- Simple chat UI without advanced HITL patterns
- No tool approval widgets or progress notifications
- Image upload support for invoices and receipts

**To use this frontend:**
- In `azure.yaml`: Ensure the `simple-chat` service is **uncommented** and `banking-web` is **commented out**
- In `app/backend/Dockerfile`: Use the CMD line:
  ```dockerfile
  CMD ["uvicorn", "app.main_handoff:app", "--host", "0.0.0.0", "--port", "8080"]
  ```

> [!IMPORTANT]
> When switching between frontends, you must update **both** the `azure.yaml` and `app/backend/Dockerfile` files to maintain compatibility. After making changes, redeploy using `azd deploy` or `azd up`.

### Agent Framework - Chatkit protocol support

This sample implements UI-to-agent communication approach built on top of the [OpenAI ChatKit protocol](https://platform.openai.com/docs/guides/chatkit) and [Microsoft Agent Framework](https://github.com/microsoft/agent-framework), specifically addressing key concerns around chatkit.js production deployment that are outlined in the [Agent Framework ChatKit integration documentation](https://github.com/microsoft/agent-framework/blob/main/python/packages/chatkit/README.md).

**Key Challenges Addressed:**

The ChatKit protocol provides a standardized chat communication pattern for AI agents, but the default implementation has several limitations for air gapped cloud:

1. **Network Dependencies**: The ChatKit frontend requires connectivity to OpenAI's CDN (`cdn.platform.openai.com`) and external services, making it unsuitable for air-gapped or highly-regulated environments
2. **Domain Registration**: Production deployments require manual domain registration at platform.openai.com

**Our Solution:**

This sample extends the Agent Framework's ChatKit integration:

- **Chatkit Protocol Compliant**: supports ChatKit server protocol specification for rich human-in-the-loop (HITL) experiences including:
  - Real-time agent progress notifications during task execution
  - Tool approval widgets for user confirmation before sensitive operations
  - Structured event streaming (thread.created, thread.item.done, etc.)
  - Support for attachments and multi-modal content (invoice images, receipts)

- **Extended Agent Framework**: Enhances the base `agent-framework-chatkit` package with:
  - Custom handoff orchestration patterns optimized for multi-agent banking workflows
  - Persistent checkpoint management for conversation state across sessions
  - Seamless integration between Agent Framework's `HandoffBuilder` and ChatKit's event streaming

- **Custom Reusable Chat Component**: Built a framework-agnostic React chat component (`banking-web/src/components/chat`) that:
  - Supports the ChatKit protocol client-side specification
  - Can be embedded into existing web applications (demonstrated in a banking app context)
  - Provides a clean API for thread management, message and attachment handling, and event callbacks

**Technical Implementation:**

The backend uses `agent-framework-chatkit` to bridge Agent Framework agents with ChatKit's protocol, implementing a custom `ChatKitServer` subclass ([chatkit_server.py](app/backend/app/routers/chatkit/chatkit_server.py)) that handles thread persistence, message conversion, and event streaming. The frontend chat component consumes the ChatKit SSE stream and renders progress indicators, approval widgets, and conversation history in a banking-integrated interface.

This approach demonstrates how to build production-grade agentic applications that combine the power of Agent Framework's orchestration capabilities with the user experience benefits of the ChatKit protocol, while maintaining full control over deployment, security, and customization requirements.

More info on the implementation can be found in the [docs/chat-server-protocol.md](docs/chat-server-protocol.md).