package com.microsoft.openai.samples.assistant.business.mcp.server;


import com.microsoft.openai.samples.assistant.business.models.Payment;
import com.microsoft.openai.samples.assistant.business.service.PaymentService;
import org.springframework.ai.tool.annotation.Tool;

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

    @Tool(description = "Submit a payment request")
    public void processPayment(Payment payment){
        logger.info("processPayment called with payment={}", payment);
        paymentService.processPayment(payment);
    }
}