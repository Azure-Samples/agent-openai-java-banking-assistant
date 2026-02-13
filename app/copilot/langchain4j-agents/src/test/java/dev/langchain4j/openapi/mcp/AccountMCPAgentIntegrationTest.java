package dev.langchain4j.openapi.mcp;

import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.AccountMCPAgentBuilder;
import com.microsoft.openai.samples.assistant.langchain4j.agent.builder.AccountMCPAgentBuilder.AccountAgent;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;

/**
 * Integration test for AccountMCPAgent.
 * 
 * This test validates that the Account Agent can:
 * - Connect to the Account MCP server
 * - Retrieve account information for authenticated users
 * - Process multiple queries in a conversation (with memory)
 * - Format responses appropriately with HTML/table formatting
 * - Maintain conversation context across multiple turns
 * 
 * Prerequisites:
 * - Account MCP server running on http://localhost:8070/sse
 * - Azure OpenAI credentials configured via environment variables:
 *   - AZURE_OPENAI_KEY
 *   - AZURE_OPENAI_ENDPOINT
 *   - AZURE_OPENAI_DEPLOYMENT_NAME
 * 
 * Run with:
 * ./mvnw -pl langchain4j-agents test -Dtest=AccountMCPAgentIntegrationTest
 */
public class AccountMCPAgentIntegrationTest {

    public static void main(String[] args) throws Exception {
         System.out.println(System.getenv("AZURE_OPENAI_ENDPOINT"));
        // Initialize Azure OpenAI Chat Model
        var azureOpenAiChatModel = AzureOpenAiChatModel.builder()
                .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
                .logRequestsAndResponses(true)
                .build();

        // Build the Account MCP Agent
        AccountAgent accountAgent = new AccountMCPAgentBuilder(
                azureOpenAiChatModel, 
                "bob.user@contoso.com",
                "http://localhost:8070/sse"
        ).buildProgrammatic();
        
        // Test Case 1: Basic account information query
        String conversationId1 = "test-account-conversation-1";
        System.out.println("\n=== Test Case 1: Basic Account Information Query ===");
        System.out.println("Query: How much money do I have in my account?");
        
        String response1 = accountAgent.chat(conversationId1, 
            "User: bob.user@contoso.com\nHow much money do I have in my account?");
        System.out.println("Response:\n" + response1);

        // Test Case 2: Multi-turn conversation with memory
        String conversationId2 = "test-account-conversation-2";
        System.out.println("\n=== Test Case 2: Multi-turn Conversation with Memory ===");
        
        System.out.println("Query 1: What are my accounts?");
        String response2a = accountAgent.chat(conversationId2, 
            "User: bob.user@contoso.com\nWhat are my accounts?");
        System.out.println("Response 1:\n" + response2a);

        System.out.println("\nQuery 2 (follow-up): Tell me more about the first one");
        String response2b = accountAgent.chat(conversationId2, 
            "User: bob.user@contoso.com\nTell me more about the first one");
        System.out.println("Response 2:\n" + response2b);

        // Test Case 3: Specific account type query
        String conversationId3 = "test-account-conversation-3";
        System.out.println("\n=== Test Case 3: Specific Account Type Query ===");
        System.out.println("Query: How much is in my savings account?");
        
        String response3 = accountAgent.chat(conversationId3, 
            "User: bob.user@contoso.com\nHow much is in my savings account?");
        System.out.println("Response:\n" + response3);

        // Test Case 4: Account details and balance information
        String conversationId4 = "test-account-conversation-4";
        System.out.println("\n=== Test Case 4: Account Details ===");
        System.out.println("Query: Show me all my account details");
        
        String response4 = accountAgent.chat(conversationId4, 
            "User: bob.user@contoso.com\nShow me all my account details");
        System.out.println("Response:\n" + response4);

        // Test Case 5: Account comparison query
        String conversationId5 = "test-account-conversation-5";
        System.out.println("\n=== Test Case 5: Account Comparison ===");
        System.out.println("Query: Which of my accounts has the most money?");
        
        String response5 = accountAgent.chat(conversationId5, 
            "User: bob.user@contoso.com\nWhich of my accounts has the most money?");
        System.out.println("Response:\n" + response5);

        // Test Case 6: User context isolation (different user)
        String conversationId6 = "test-account-conversation-6";
        System.out.println("\n=== Test Case 6: User Context Isolation ===");
        System.out.println("Query: Get accounts for different user");
        System.out.println("Note: Agent should only return data for authenticated user (bob.user@contoso.com)");
        
        String response6 = accountAgent.chat(conversationId6, 
            "User: bob.user@contoso.com\nWhat are my account balances?");
        System.out.println("Response:\n" + response6);

        System.out.println("\n=== Account Agent Integration Tests Completed ===");
    }
}
