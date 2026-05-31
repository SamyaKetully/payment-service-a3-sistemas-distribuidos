package com.samyasotero.testpaymentservice.strategy;


import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("PIX")
public class PixStrategy implements PaymentStrategy {

    @Override
    public void processPayment(Payment payment) {
        System.out.println("Gerando QR Code PIX para o pedido: " + payment.getOrderId());

        payment.setProviderRef("PIX-REF-" + UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.PENDING);
    }
}
