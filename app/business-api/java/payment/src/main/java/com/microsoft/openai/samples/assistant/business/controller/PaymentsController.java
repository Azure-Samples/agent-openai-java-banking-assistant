package com.microsoft.openai.samples.assistant.business.controller;

import com.microsoft.openai.samples.assistant.business.models.Payment;
import com.microsoft.openai.samples.assistant.business.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentsController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentsController.class);

    private final PaymentService paymentService;

    public PaymentsController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public void submitPayment(@RequestBody Payment payment) {
        logger.info("Received payment request: {}", payment);
        paymentService.processPayment(payment);
    }
}