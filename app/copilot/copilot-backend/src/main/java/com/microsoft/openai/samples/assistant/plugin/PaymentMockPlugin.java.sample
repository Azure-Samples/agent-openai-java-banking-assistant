package com.microsoft.openai.samples.assistant.plugin;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;

public class PaymentMockPlugin {


    @DefineKernelFunction(name = "payBill", description = "Gets the last payment transactions based on the payee, recipient name")
    public String submitBillPayment(
            @KernelFunctionParameter(name = "recipientName", description = "Name of the payee, recipient") String recipientName,
            @KernelFunctionParameter(name = "documentId", description = " the bill id or invoice number") String documentID,
            @KernelFunctionParameter(name = "amount", description = "the total amount to pay") String amount) {

        System.out.println("Bill payment executed for recipient: " + recipientName + " with documentId: " + documentID + " and amount: " + amount);

        return "Payment Successful";

    }

}

