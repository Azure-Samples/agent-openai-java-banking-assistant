package com.microsoft.openai.samples.assistant.business.service;

import com.microsoft.openai.samples.assistant.business.models.Payment;
import com.microsoft.openai.samples.assistant.business.models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final WebClient.Builder webClientBuilder;
    private final String transactionAPIUrl;

    public PaymentService(WebClient.Builder webClientBuilder, @Value("${transactions.api.url}") String transactionAPIUrl) {
        this.webClientBuilder = webClientBuilder;
        this.transactionAPIUrl = transactionAPIUrl;
    }

    public void processPayment(Payment payment) {

        if (payment.accountId() == null || payment.accountId().isEmpty())
            throw new IllegalArgumentException("AccountId is empty or null");
        try {
            Integer.parseInt(payment.accountId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("AccountId is not a valid number");
        }

        if (!payment.paymentType().equalsIgnoreCase("transfer") && (payment.paymentMethodId() == null || payment.paymentMethodId().isEmpty()))
            throw new IllegalArgumentException("paymentMethodId is empty or null");

        try {
            Integer.parseInt(payment.paymentMethodId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("paymentMethodId is not a valid number");
        }


        // Log the payment details
        logger.info("Payment successful for: {}", payment.toString());

        // Convert the Payment object into a Transaction object
        Transaction transaction = convertPaymentToTransaction(payment);

        /**
         * Make the POST request. The transaction is sent to the transaction API. In a real scenario this would be an event published to a hub and consumed by the transaction API.
         */

        logger.info("Notifying payment [{}] for account[{}]..", payment.description() , transaction.accountId());
        webClientBuilder.build()
                .post()
                .uri(transactionAPIUrl + "/transactions/{accountId}", payment.accountId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(transaction))
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(response -> logger.info("Transaction notified for: {}", transaction.toString()));
    }

    private Transaction convertPaymentToTransaction(Payment payment) {
        return new Transaction(
                UUID.randomUUID().toString(),
                payment.description(),
                "outcome",
                payment.recipientName(),
                payment.recipientBankCode(),
                payment.accountId(),
                payment.paymentType(),
                payment.amount(),
                payment.timestamp()
        );
    }
}