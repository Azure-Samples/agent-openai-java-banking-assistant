package com.microsoft.openai.samples.assistant.business.controller;

import com.microsoft.openai.samples.assistant.business.models.Account;
import com.microsoft.openai.samples.assistant.business.models.PaymentMethod;
import com.microsoft.openai.samples.assistant.business.models.Beneficiary;
import com.microsoft.openai.samples.assistant.business.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountId}")
    public Account getAccountDetails(@PathVariable String accountId) {
        logger.info("Received request to get account details for account id: {}", accountId);
        return accountService.getAccountDetails(accountId);
    }

    @GetMapping("/{accountId}/paymentmethods/{methodId}")
    public PaymentMethod getPaymentMethodDetails(@PathVariable String accountId, @PathVariable String methodId) {
        logger.info("Received request to get payment method details for account id: {} and method id: {}", accountId, methodId);
        return accountService.getPaymentMethodDetails(methodId);
    }

    @GetMapping("/{accountId}/registeredBeneficiaries")
    public List<Beneficiary> getBeneficiaryDetails(@PathVariable String accountId) {
        logger.info("Received request to get beneficiary details for account id: {}", accountId);
        return accountService.getRegisteredBeneficiary(accountId);
    }
}