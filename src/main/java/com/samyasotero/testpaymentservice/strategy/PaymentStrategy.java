package com.samyasotero.testpaymentservice.strategy;
import com.samyasotero.testpaymentservice.model.Payment;

public interface PaymentStrategy {

    void processPayment(Payment payment);
}
