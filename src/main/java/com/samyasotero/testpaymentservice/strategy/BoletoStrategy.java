package com.samyasotero.testpaymentservice.strategy;

import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component("BOLETO")
public class BoletoStrategy implements PaymentStrategy {

    @Override
    public void processPayment(Payment payment) {
        System.out.println("Registrando Boleto na CIP para o pedido: " + payment.getOrderId());

        // Simulação de geração de código de barras
        payment.setProviderRef("BOL-REF-" + UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.PENDING);
    }
}
