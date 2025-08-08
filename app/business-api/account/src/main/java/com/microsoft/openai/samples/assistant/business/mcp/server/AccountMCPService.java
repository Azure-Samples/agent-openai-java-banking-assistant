package com.microsoft.openai.samples.assistant.business.mcp.server;

import com.microsoft.openai.samples.assistant.business.models.Account;
import com.microsoft.openai.samples.assistant.business.models.Beneficiary;
import com.microsoft.openai.samples.assistant.business.models.PaymentMethod;
import com.microsoft.openai.samples.assistant.business.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

@Service
public class AccountMCPService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountMCPService.class);

    private final AccountService accountService;
    public AccountMCPService(AccountService accountService) {
       this.accountService = accountService;
    }

    @Tool(description = "Get account details and available payment methods")
    public Account getAccountDetails(@ToolParam(description = "The account id of the user") String accountId) {
        LOGGER.debug("Fetching account details for accountId: {}", accountId);
        return this.accountService.getAccountDetails(accountId);

    }

    @Tool(description = "Get payment method detail with available balance")
    public PaymentMethod getPaymentMethodDetails(@ToolParam(description = "The payment method id") String paymentMethodId) {
        LOGGER.debug("Fetching payment method details for payment method id: {}", paymentMethodId);
       return this.accountService.getPaymentMethodDetails(paymentMethodId);
    }

    @Tool(description = "Get list of registered beneficiaries for a specific account")
    public List<Beneficiary> getRegisteredBeneficiary(String accountId) {
     return this.accountService.getRegisteredBeneficiary(accountId);
    }
}