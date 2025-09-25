package com.microsoft.openai.samples.assistant.business.mcp.server;

import com.microsoft.openai.samples.assistant.business.models.Account;
import com.microsoft.openai.samples.assistant.business.models.Beneficiary;
import com.microsoft.openai.samples.assistant.business.models.PaymentMethod;
import com.microsoft.openai.samples.assistant.business.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.ai.tool.annotation.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class AccountMCPService {

    private static final Logger logger = LoggerFactory.getLogger(AccountMCPService.class);

    private final AccountService accountService;
    public AccountMCPService(AccountService accountService) {
       this.accountService = accountService;
    }

    @Tool(description = "Get account details and available payment methods")
    public Account getAccountDetails(String accountId) {
        logger.info("Request to getAccountDetails with accountId: {}", accountId);
        return this.accountService.getAccountDetails(accountId);

    }

    @Tool(description = "Get payment method detail with available balance")
    public PaymentMethod getPaymentMethodDetails(String paymentMethodId) {
        logger.info("Request to getPaymentMethodDetails with paymentMethodId: {}", paymentMethodId);
       return this.accountService.getPaymentMethodDetails(paymentMethodId);
    }

    @Tool(description = "Get list of registered beneficiaries for a specific account")
    public List<Beneficiary> getRegisteredBeneficiary(String accountId) {
        logger.info("Request to getRegisteredBeneficiary with accountId: {}", accountId);
     return this.accountService.getRegisteredBeneficiary(accountId);
    }
}