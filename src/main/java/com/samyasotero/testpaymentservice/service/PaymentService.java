package com.samyasotero.testpaymentservice.service;

import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentMethod;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import com.samyasotero.testpaymentservice.repository.PaymentRepository;
import com.samyasotero.testpaymentservice.strategy.PaymentStrategy;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.Map;

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Map<String, PaymentStrategy> paymentStrategies;

    public PaymentService(PaymentRepository paymentRepository, Map<String, PaymentStrategy> paymentStrategies) {
        this.paymentRepository = paymentRepository;
        this.paymentStrategies = paymentStrategies;
    }

    @Transactional
    public Payment process(Payment payment) {
        payment.setStatus(PaymentStatus.PENDING);

        // 1. Pegamos o método que o usuário enviou (como String)
        String metodoEnviado = payment.getPaymentMethod().name(); // ou apenas getPaymentMethod() se já for String

        // 2. Comparamos se o método enviado NÃO existe dentro do nosso Enum PaymentMethod
        boolean metodoValido = Arrays.stream(PaymentMethod.values())
                .anyMatch(enumItem -> enumItem.name().equals(metodoEnviado));

        // 3. O nosso novo IF: Se não for válido (diferente dos itens do Enum), lançamos o erro
        if (!metodoValido) {
            throw new IllegalArgumentException(
                    "Método de pagamento '" + metodoEnviado + "' não suportado. " +
                            "Opções válidas: " + Arrays.toString(PaymentMethod.values())
            );
        }

        PaymentStrategy strategy = paymentStrategies.get(metodoEnviado);

        if (strategy == null) {
            throw new IllegalStateException("Estratégia de pagamento não implementada no sistema.");
        }

        strategy.processPayment(payment);
        paymentRepository.save(payment);

        return payment;
    }
}
