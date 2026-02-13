# Migration Guide: Custom Agent Framework → Langchain4j-Agentic

## Overview

This guide details the migration from our custom agent framework implementation to the official **langchain4j-agentic** module. The migration improves maintainability, leverages official framework features, and provides better integration with the langchain4j ecosystem.

## Table of Contents

- [Why Migrate?](#why-migrate)
- [Prerequisites](#prerequisites)
- [Migration Phases](#migration-phases)
- [Detailed Implementation](#detailed-implementation)
- [Testing Strategy](#testing-strategy)
- [Rollback Plan](#rollback-plan)
- [FAQ](#faq)

---

## Why Migrate?

### Benefits

1. **Official Support**: Use officially maintained and tested code
2. **Feature Rich**: Access to declarative APIs, multiple supervisor strategies, workflow patterns
3. **Better Tooling**: Built-in observability, error handling, and debugging
4. **Reduced Maintenance**: No need to maintain custom ReAct loop implementation
5. **Community Updates**: Automatic access to new features and improvements

### What Changes

| Component | Before (Custom) | After (Agentic) |
|-----------|----------------|-----------------|
| Agent Definition | Extend `AbstractReActAgent` | Use `@Agent` annotation or builder |
| Supervisor | Custom routing class | `SupervisorAgentService` |
| Tool Execution | Manual ReAct loop | Automatic tool invocation |
| State Management | Manual `List<ChatMessage>` | `AgenticScope` |
| Return Types | `List<ChatMessage>` | `String` or typed outputs |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Langchain4j version: **1.0.0-beta2** or higher
- Understanding of Spring dependency injection
- Familiarity with MCP (Model Context Protocol)

---

## Migration Phases

### Phase 1: Preparation ✅
- [x] Analyze current implementation
- [x] Identify all affected files
- [x] Document migration strategy
- [x] Create migration guide

### Phase 2: Dependencies 🔄
- [ ] Add `langchain4j-agentic` dependency
- [ ] Verify no dependency conflicts
- [ ] Build project successfully

### Phase 3: Agent Refactoring 🔄
- [ ] Refactor `AccountMCPAgent`
- [ ] Refactor `TransactionHistoryMCPAgent`
- [ ] Refactor `PaymentMCPAgent`

### Phase 4: Supervisor Migration 🔄
- [ ] Migrate `SupervisorAgent` to agentic

### Phase 5: Integration 🔄
- [ ] Update `MCPAgentsConfiguration`
- [ ] Update `ChatController`
- [ ] Wire all components together

### Phase 6: Cleanup 🔄
- [ ] Delete custom agent framework classes
- [ ] Update all imports
- [ ] Remove unused code

### Phase 7: Testing 🔄
- [ ] Update integration tests
- [ ] Run all test suites
- [ ] Perform end-to-end testing

### Phase 8: Documentation 🔄
- [ ] Update README
- [ ] Update architecture diagrams
- [ ] Document new patterns

---

## Detailed Implementation

### Step 1: Add Dependencies

**File**: `app/copilot/langchain4j-agents/pom.xml`

Add the following dependency:

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-agentic</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
```

**Verify**:
```bash
cd app/copilot/langchain4j-agents
mvn clean compile
```

---

### Step 2: Refactor AccountMCPAgent

**Old Implementation** (`AccountMCPAgent.java`):
```java
public class AccountMCPAgent extends MCPToolAgent {
    public AccountMCPAgent(ChatLanguageModel chatModel, String loggedUserName, String accountMCPServerUrl) {
        super(chatModel, List.of(new MCPServerMetadata("account", accountMCPServerUrl, MCPProtocolType.SSE)));
        // ... initialization
    }
    
    @Override
    public List<ChatMessage> invoke(List<ChatMessage> chatHistory) throws AgentExecutionException {
        // Custom logic
    }
}
```

**New Implementation** (Option A - Declarative):
```java
public interface AccountMCPAgent {
    
    @UserMessage("""
        You are a personal financial advisor who helps retrieve bank account information.
        Use HTML list or table to display account information.
        Always use the below logged user details: '{{loggedUserName}}'
        """)
    @Agent(description = "Personal financial advisor for retrieving bank account information")
    String assist(@V("loggedUserName") String userName, @V("request") String request);
    
    @ChatModelSupplier
    static ChatModel chatModel(ChatLanguageModel model) {
        return model;
    }
    
    @ToolsSupplier
    static List<ToolSpecification> tools(String accountMCPServerUrl) {
        McpClient client = createMcpClient(accountMCPServerUrl);
        return client.listTools();
    }
    
    private static McpClient createMcpClient(String url) {
        McpTransport transport = new HttpMcpTransport.Builder()
            .sseUrl(url)
            .logRequests(true)
            .logResponses(true)
            .timeout(Duration.ofHours(3))
            .build();
            
        return new DefaultMcpClient.Builder()
            .transport(transport)
            .build();
    }
}
```

**New Implementation** (Option B - Programmatic):
```java
public class AccountMCPAgentBuilder {
    
    public static Object buildAccountAgent(
            ChatLanguageModel chatModel, 
            String loggedUserName, 
            String accountMCPServerUrl) {
        
        // Create MCP client
        McpClient mcpClient = createMcpClient(accountMCPServerUrl);
        
        // Build agent
        return AgenticServices.agentBuilder(AccountAgentInterface.class)
            .chatModel(chatModel)
            .tools(mcpClient.listTools())
            .description("Personal financial advisor for account information")
            .build();
    }
    
    public interface AccountAgentInterface {
        @UserMessage("{{systemMessage}}\n\nUser request: {{request}}")
        String assist(@V("systemMessage") String systemMsg, @V("request") String request);
    }
    
    private static McpClient createMcpClient(String url) {
        McpTransport transport = new HttpMcpTransport.Builder()
            .sseUrl(url)
            .logRequests(true)
            .logResponses(true)
            .timeout(Duration.ofHours(3))
            .build();
            
        return new DefaultMcpClient.Builder()
            .transport(transport)
            .build();
    }
    
    public static String getSystemMessage(String loggedUserName) {
        return """
            You are a personal financial advisor who helps retrieve bank account information.
            Use HTML list or table to display account information.
            Always use the below logged user details: '%s'
            """.formatted(loggedUserName);
    }
}
```

---

### Step 3: Refactor TransactionHistoryMCPAgent

Follow similar pattern as AccountMCPAgent:

```java
public class TransactionHistoryMCPAgentBuilder {
    
    public static Object buildTransactionAgent(
            ChatLanguageModel chatModel,
            String loggedUserName,
            String transactionMCPServerUrl,
            String accountMCPServerUrl) {
        
        // Create MCP clients for both services
        McpClient transactionClient = createMcpClient(transactionMCPServerUrl);
        McpClient accountClient = createMcpClient(accountMCPServerUrl);
        
        // Combine tools from both clients
        List<ToolSpecification> allTools = new ArrayList<>();
        allTools.addAll(transactionClient.listTools());
        allTools.addAll(accountClient.listTools());
        
        return AgenticServices.agentBuilder(TransactionAgentInterface.class)
            .chatModel(chatModel)
            .tools(allTools)
            .description("Personal financial advisor for transaction history")
            .build();
    }
    
    public interface TransactionAgentInterface {
        @UserMessage("{{systemMessage}}\n\nUser request: {{request}}")
        String assist(@V("systemMessage") String systemMsg, @V("request") String request);
    }
}
```

---

### Step 4: Refactor PaymentMCPAgent

PaymentMCPAgent is more complex as it includes custom tools (InvoiceScanTool):

```java
public class PaymentMCPAgentBuilder {
    
    public static Object buildPaymentAgent(
            ChatLanguageModel chatModel,
            DocumentIntelligenceInvoiceScanHelper scanHelper,
            String loggedUserName,
            String transactionMCPServerUrl,
            String accountMCPServerUrl,
            String paymentsMCPServerUrl) {
        
        // Create MCP clients
        McpClient paymentClient = createMcpClient(paymentsMCPServerUrl);
        McpClient transactionClient = createMcpClient(transactionMCPServerUrl);
        McpClient accountClient = createMcpClient(accountMCPServerUrl);
        
        // Combine all MCP tools
        List<ToolSpecification> allTools = new ArrayList<>();
        allTools.addAll(paymentClient.listTools());
        allTools.addAll(transactionClient.listTools());
        allTools.addAll(accountClient.listTools());
        
        // Add custom InvoiceScanTool
        InvoiceScanTool invoiceTool = new InvoiceScanTool(scanHelper);
        ToolSpecification invoiceToolSpec = ToolSpecifications.toolSpecificationFrom(invoiceTool);
        allTools.add(invoiceToolSpec);
        
        return AgenticServices.agentBuilder(PaymentAgentInterface.class)
            .chatModel(chatModel)
            .tools(allTools)
            .tools(invoiceTool) // Add the actual tool object for execution
            .description("Personal financial advisor for bill payments")
            .build();
    }
    
    public interface PaymentAgentInterface {
        @UserMessage("{{systemMessage}}\n\nUser request: {{request}}")
        String assist(@V("systemMessage") String systemMsg, @V("request") String request);
    }
}
```

---

### Step 5: Migrate SupervisorAgent

**Old Implementation**:
```java
public class SupervisorAgent {
    private final ChatLanguageModel chatLanguageModel;
    private final List<Agent> agents;
    
    public List<ChatMessage> invoke(List<ChatMessage> chatHistory) {
        // Manual routing logic
        String nextAgent = selectAgent(chatHistory);
        return routeToAgent(nextAgent, chatHistory);
    }
}
```

**New Implementation**:
```java
public class SupervisorAgentBuilder {
    
    public static Object buildSupervisor(
            ChatLanguageModel chatModel,
            Object accountAgent,
            Object transactionAgent,
            Object paymentAgent) {
        
        return AgenticServices.supervisorBuilder()
            .chatModel(chatModel)
            .responseStrategy(SupervisorResponseStrategy.LAST)
            .subAgents(accountAgent, transactionAgent, paymentAgent)
            .description("Banking customer support supervisor")
            .build();
    }
}
```

---

### Step 6: Update MCPAgentsConfiguration

**File**: `app/copilot/copilot-backend/src/main/java/com/microsoft/openai/samples/assistant/config/MCPAgentsConfiguration.java`

```java
@Configuration
public class MCPAgentsConfiguration {
    
    @Value("${transactions.api.url}") String transactionsMCPServerUrl;
    @Value("${accounts.api.url}") String accountsMCPServerUrl;
    @Value("${payments.api.url}") String paymentsMCPServerUrl;

    private final ChatLanguageModel chatLanguageModel;
    private final LoggedUserService loggedUserService;
    private final DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper;

    public MCPAgentsConfiguration(ChatLanguageModel chatLanguageModel, 
                                 LoggedUserService loggedUserService, 
                                 DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper) {
        this.chatLanguageModel = chatLanguageModel;
        this.loggedUserService = loggedUserService;
        this.documentIntelligenceInvoiceScanHelper = documentIntelligenceInvoiceScanHelper;
    }
    
    @Bean
    public Object accountAgent() {
        return AccountMCPAgentBuilder.buildAccountAgent(
            chatLanguageModel,
            loggedUserService.getLoggedUser().username(),
            accountsMCPServerUrl
        );
    }

    @Bean
    public Object transactionAgent() {
        return TransactionHistoryMCPAgentBuilder.buildTransactionAgent(
            chatLanguageModel,
            loggedUserService.getLoggedUser().username(),
            transactionsMCPServerUrl,
            accountsMCPServerUrl
        );
    }

    @Bean
    public Object paymentAgent() {
        return PaymentMCPAgentBuilder.buildPaymentAgent(
            chatLanguageModel,
            documentIntelligenceInvoiceScanHelper,
            loggedUserService.getLoggedUser().username(),
            transactionsMCPServerUrl,
            accountsMCPServerUrl,
            paymentsMCPServerUrl
        );
    }

    @Bean
    public Object supervisorAgent() {
        return SupervisorAgentBuilder.buildSupervisor(
            chatLanguageModel,
            accountAgent(),
            transactionAgent(),
            paymentAgent()
        );
    }
}
```

---

### Step 7: Update ChatController

**File**: `app/copilot/copilot-backend/src/main/java/com/microsoft/openai/samples/assistant/controller/ChatController.java`

The challenge here is adapting from `List<ChatMessage>` to the agentic API.

**Option A: Simple String-based Approach**:
```java
@RestController
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
    private final Object supervisorAgent; // Changed from SupervisorAgent

    public ChatController(Object supervisorAgent){
        this.supervisorAgent = supervisorAgent;
    }

    @PostMapping(value = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> openAIAsk(@RequestBody ChatAppRequest chatRequest) {
        if (chatRequest.stream()) {
            LOGGER.warn("Requested streaming with application/json content-type");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Streaming requires application/ndjson content-type");
        }

        if (chatRequest.messages() == null || chatRequest.messages().isEmpty()) {
            LOGGER.warn("history cannot be null in Chat request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // Get the last user message
        String userRequest = getLastUserMessage(chatRequest);
        
        LOGGER.debug("Processing chat request: {}", userRequest);

        // Invoke supervisor (now returns String)
        String response = invokeSupervisor(userRequest);

        return ResponseEntity.ok(ChatResponse.buildFromText(response));
    }

    private String getLastUserMessage(ChatAppRequest chatRequest) {
        return chatRequest.messages().stream()
            .filter(msg -> "user".equals(msg.role()))
            .reduce((first, second) -> second) // Get last
            .map(msg -> {
                String content = msg.content();
                if (msg.attachments() != null && !msg.attachments().isEmpty()) {
                    content += " " + msg.attachments().toString();
                }
                return content;
            })
            .orElse("");
    }

    private String invokeSupervisor(String request) {
        // Use reflection to call the invoke method
        try {
            Method invokeMethod = supervisorAgent.getClass().getMethod("invoke", String.class);
            return (String) invokeMethod.invoke(supervisorAgent, request);
        } catch (Exception e) {
            LOGGER.error("Error invoking supervisor agent", e);
            throw new RuntimeException("Failed to process request", e);
        }
    }
}
```

**Option B: With AgenticScope for State Management**:
```java
@RestController
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
    private final AgentExecutor supervisorAgent; // Use AgentExecutor
    private final Map<String, DefaultAgenticScope> sessionScopes = new ConcurrentHashMap<>();

    public ChatController(Object supervisorAgent){
        this.supervisorAgent = (AgentExecutor) supervisorAgent;
    }

    @PostMapping(value = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> openAIAsk(@RequestBody ChatAppRequest chatRequest) {
        // ... validation ...

        List<ChatMessage> chatHistory = convertToLangchain4j(chatRequest);
        
        // Create or get session scope
        String sessionId = getOrCreateSessionId(chatRequest);
        DefaultAgenticScope scope = sessionScopes.computeIfAbsent(
            sessionId, 
            k -> new DefaultAgenticScope()
        );

        // Store chat history in scope
        scope.writeState("chatHistory", chatHistory);
        
        // Execute agent
        Object response = supervisorAgent.execute(scope, null);

        return ResponseEntity.ok(ChatResponse.buildFromText(response.toString()));
    }
}
```

---

### Step 8: Delete Custom Framework Classes

Once all agents are migrated and tested, delete:

```bash
# From app/copilot/langchain4j-agents/src/main/java/com/microsoft/langchain4j/agent/
rm Agent.java
rm AgentMetadata.java
rm AgentExecutionException.java
rm AbstractReActAgent.java

# From app/copilot/langchain4j-agents/src/main/java/com/microsoft/langchain4j/agent/mcp/
rm MCPToolAgent.java
rm MCPServerMetadata.java
rm MCPProtocolType.java
```

---

### Step 9: Update Tests

Each test file needs updates. Example for `AccountMCPAgentIntegrationTest`:

**Before**:
```java
AccountMCPAgent agent = new AccountMCPAgent(chatModel, username, serverUrl);
List<ChatMessage> response = agent.invoke(chatHistory);
```

**After**:
```java
Object agent = AccountMCPAgentBuilder.buildAccountAgent(chatModel, username, serverUrl);
String response = invokeAgent(agent, userRequest);
```

---

## Testing Strategy

### Unit Tests
- Test each agent builder independently
- Verify tool integration
- Test supervisor routing logic

### Integration Tests
- Test end-to-end conversation flows
- Verify MCP tool execution
- Test error handling

### Manual Testing
- Deploy locally with Docker Compose
- Test through UI
- Verify payment flows with invoice upload

---

## Rollback Plan

If migration fails:

1. **Revert Git Changes**:
   ```bash
   git checkout main
   ```

2. **Restore Dependencies**:
   ```bash
   git checkout pom.xml
   mvn clean install
   ```

3. **Redeploy**:
   ```bash
   azd deploy
   ```

---

## FAQ

### Q: Why not use declarative agents throughout?
**A**: MCP client initialization requires programmatic setup. Declarative agents work better for simple use cases.

### Q: What happens to chat history?
**A**: Chat history can be managed via `AgenticScope` or simplified to pass only the last user message, depending on requirements.

### Q: Are all custom tools supported?
**A**: Yes, custom tools like `InvoiceScanTool` can be integrated alongside MCP tools.

### Q: What about streaming responses?
**A**: Langchain4j-agentic supports streaming. Implementation requires updating `ChatController` to use `TokenStream`.

### Q: Performance impact?
**A**: Minimal. The agentic framework is optimized and may actually improve performance through better tool caching.

---

## Additional Resources

- [Langchain4j Agentic Documentation](https://github.com/langchain4j/langchain4j/tree/main/langchain4j-agentic)
- [Supervisor Agent Examples](https://github.com/langchain4j/langchain4j/tree/main/langchain4j-agentic/src/test/java/dev/langchain4j/agentic)
- [MCP Integration Guide](https://docs.langchain4j.dev/integrations/mcp)

---

## Migration Checklist

- [ ] Phase 1: Preparation complete
- [ ] Phase 2: Dependencies added
- [ ] Phase 3: All agents refactored
- [ ] Phase 4: Supervisor migrated
- [ ] Phase 5: Configuration updated
- [ ] Phase 6: Custom classes deleted
- [ ] Phase 7: All tests passing
- [ ] Phase 8: Documentation updated
- [ ] Phase 9: Deployed and verified

---

**Last Updated**: February 4, 2026  
**Version**: 1.0  
**Status**: In Progress
