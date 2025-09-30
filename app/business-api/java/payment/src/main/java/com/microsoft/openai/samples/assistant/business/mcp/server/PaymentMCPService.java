package com.microsoft.openai.samples.assistant.business.mcp.server;


import com.microsoft.openai.samples.assistant.business.models.Payment;
import com.microsoft.openai.samples.assistant.business.service.PaymentService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentMCPService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMCPService.class);

    private final PaymentService paymentService;

    public PaymentMCPService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
    @Tool(description = "Submit a payment request")
    public void processPayment(Payment payment){
        logger.info("processPayment called with payment={}", payment);
        paymentService.processPayment(payment);
    }
    */
    @Tool(description = "Submit a payment request with flattened parameters")
    public void processPayment(
            @ToolParam(description = "Payment description") String description,
            @ToolParam(description = "Recipient name") String recipientName,
            @ToolParam(description = "Recipient bank code (optional)", required = false) String recipientBankCode,
            @ToolParam(description = "Account ID") String accountId,
            @ToolParam(description = "Payment method ID") String paymentMethodId,
            @ToolParam(description = "Payment type") String paymentType,
            @ToolParam(description = "Amount") String amount,
            @ToolParam(description = "Timestamp") String timestamp) {
        logger.info("processPaymentFlat called with description={}, recipientName={}, recipientBankCode={}, accountId={}, paymentMethodId={}, paymentType={}, amount={}, timestamp={}",
                description, recipientName, recipientBankCode, accountId, paymentMethodId, paymentType, amount, timestamp);
        Payment payment = new Payment(description, recipientName, recipientBankCode, accountId, paymentMethodId, paymentType, amount, timestamp);
        paymentService.processPayment(payment);
    }

}