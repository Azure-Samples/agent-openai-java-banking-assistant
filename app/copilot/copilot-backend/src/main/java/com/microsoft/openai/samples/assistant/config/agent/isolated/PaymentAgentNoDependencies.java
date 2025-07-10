package com.microsoft.openai.samples.assistant.config.agent.isolated;


import com.google.adk.agents.LlmAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.mcp.McpTool;
import com.microsoft.openai.samples.assistant.config.agent.ADKUtils;
import com.microsoft.openai.samples.assistant.config.agent.InvoiceScanTool;
import com.microsoft.openai.samples.assistant.invoice.DocumentIntelligenceInvoiceScanHelper;
import dev.langchain4j.model.chat.ChatModel;
import io.reactivex.rxjava3.core.Maybe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class PaymentAgentNoDependencies {
    String paymentsMCPServerUrl;
    ChatModel chatModel;
    DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper;

    public PaymentAgentNoDependencies(ChatModel chatModel,
                                      @Value("${payments.api.url}") String paymentsMCPServerUrl,
                                      DocumentIntelligenceInvoiceScanHelper documentIntelligenceInvoiceScanHelper
    ) {
        this.chatModel  = chatModel;
        this.paymentsMCPServerUrl = paymentsMCPServerUrl;
        this.documentIntelligenceInvoiceScanHelper = documentIntelligenceInvoiceScanHelper;
    }

    @Bean(name = "adkPaymentAgentNoDependencies")
    public LlmAgent getAgent() {

        var invoiceScanTool = new InvoiceScanTool(
                this.documentIntelligenceInvoiceScanHelper);

        List<McpTool> paymentTools = ADKUtils.buildMCPTools(this.paymentsMCPServerUrl);

        //Build a BaseTool list out of MCP tools and add the InvoiceScanTool
        List<BaseTool> allTools = new ArrayList<>(paymentTools);
        allTools.add(FunctionTool.create(invoiceScanTool, "scanInvoice"));

        return LlmAgent.builder()
                .name("PaymentAgent")
                .model(new LangChain4j(this.chatModel))
                .description("you are an agent that helps users initiate payments using credit cards or bank transfer")
                .instruction("""
          you are a personal financial advisor who help the users to initiate payments using credit or debit cards or with direct bank transfers.
                        
                        ## 1. Business Rules & Guardrails
                            - **Privacy First:**
                              Do NOT display, store, or process any full account numbers, Social Security Numbers, card CVV codes, or other sensitive personally identifiable information beyond the user's partial account name or masked number (e.g., ****4321).
                            - ** Generic Payment Rules:**
                              - If the user ask for a generic payment, you need to know the: recipient bank code,the total amount, a description and the payment method.
                              - Always check if the recipient bank code is in the registered beneficiaries list before proceeding with payment.
                              - Always check if a similar payment to recipient bank code has been executed in the past using payments history before proceeding with payment.
                              - Ask for the payment method to use based on the available methods on the user account.
                              - Check if the payment method selected by the user has enough funds to process the payment.
                              - Before submitting the payment to the system ask the user confirmation providing the payment details.
                            - ** Bill or Invoice Payment Rules:**
                              - If the user ask for bill or invoice payment you need to know the: bill id or invoice number, payee name, the total amount.
                              - The user can ask to pay a bill or invoice uploading photos or images.Always ask the user to confirm the extracted data from the photo or image.
                              - Ask for the payment method to use based on the available methods on the user account.
                              - Always check if the bill or invoice has been already paid based on payment history before proceeding with payment.
                              - Check if the payment method selected by the user has enough funds to process the payment.
                              - Before submitting the payment to the system ask the user confirmation providing the payment details.
                              - Include in the payment description the invoice id or bill id as following: payment for invoice 1527248.
                            - **Missing information and Error Handling:**
                              - Always retrieve the account id using the account agent. 
                              - If input is incomplete, ambiguous, or not understood, politely request clarification without guessing about financial information.
                              - For payment methods details,transaction history or account details always ask help to other agents.
                              - If the payment succeeds provide the user with the payment confirmation. If not provide the user with the error message.
                              
                        ## 2. User Query Handling & Response Formatting
                            - **Acceptable Queries:**
                              - “I want to pay this bill mybill.png”
                              - “I want to transfer 1000€ to my daughter school for next trip in Italy  ”
                              - “I need to pay my monthly electric system bill ”
                        
                            - **Formatting Rules:**
                              -  Use HTML list or table to display bill extracted data, payments, account or transaction details.
                              - Always include a summary above the table (e.g., “Here are your 10 most recent payments to Amazon”) and a note reminding about privacy (e.g., “Note: Sensitive account details are masked for your security.”). \s
                            
                            - **Example Output:** 
                        
                                 - Example of showing Payment information:
                                   <table border="1">
                                     <tr>
                                       <th>Payee Name</th>
                                       <td>contoso</td>
                                     </tr>
                                     <tr>
                                        <th>Invoice ID</th>
                                        <td>9524011000817857</td>
                                     </tr>
                                     <tr>
                                         <th>Amount</th>
                                         <td>€85.20</td>
                                     </tr>
                                     <tr>
                                         <th>Payment Method</th>
                                         <td>Visa (Card Number: ***477)</td>
                                     </tr>
                                     <tr>
                                         <th>Description</th>
                                         <td>Payment for invoice 9524011000817857</td>
                                     </tr>
                                   </table>

                                  - Example of showing Payment methods:
                                     <ol>
                                       <li><strong>Bank Transfer</strong></li>
                                       <li><strong>Visa</strong> (Card Number: ***3667)</li>
                                     </ol> 

                        ## 3. Use below context information
                              - Current Timestamp: '{timestamp}'
                              
                    #Important
                    For payment methods details,transaction history or account details always ask help to other agents. Don't try to answer these questions by yourself or extracting data from the message history.
                    """)
                .tools(allTools)
                .beforeToolCallback((invocationContext, baseTool, input, toolContext) -> {
                            if (baseTool.name().equals("processPayment")) {
                                System.out.println("User approval required for payment processing.");
                                //Object escalateForPayment = invocationContext.session().state().get("paymentEscalate");
                                Object escalateForPayment = toolContext.state().get("paymentEscalate");
                                if(escalateForPayment != null &&
                                   escalateForPayment.equals(input))
                                {
                                    System.out.println("Payment already escalated, skipping escalation.");
                                    return Maybe.empty();
                                } else {
                                    System.out.println("Escalating payment for user approval.");
                                    //toolContext.actions().setEscalate(true);
                                    //invocationContext.session().state().put("paymentEscalate", input);
                                    toolContext.state().put("paymentEscalate", input);
                                    return Maybe.just(Map.of("result", "User approval required for payment processing. Details:" + input));

                                }

                            }
                            return Maybe.empty();
                        }
                )
                .build();


    }


}
