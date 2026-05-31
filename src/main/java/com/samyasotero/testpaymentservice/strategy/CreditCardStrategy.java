package com.samyasotero.testpaymentservice.strategy;

import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component("CREDIT_CARD")
public class CreditCardStrategy implements PaymentStrategy {

    @Override
    public void processPayment(Payment payment) {
        System.out.println("Processando Cartão de Crédito na adquirente para o pedido: " + payment.getOrderId());

        payment.setProviderRef("CC-REF-" + UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.APPROVED);
    }
}
