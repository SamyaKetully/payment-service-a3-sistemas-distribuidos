package com.samyasotero.testpaymentservice.service;

import com.samyasotero.testpaymentservice.dto.ProcessPaymentDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import com.samyasotero.testpaymentservice.strategy.PaymentStrategy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentTransactionService transactionService;
    private final PaymentEventPublisher eventPublisher;
    private final Map<String, PaymentStrategy> paymentStrategies;

    public PaymentService(PaymentTransactionService transactionService,
                          PaymentEventPublisher eventPublisher,
                          Map<String, PaymentStrategy> paymentStrategies) {
        this.transactionService = transactionService;
        this.eventPublisher = eventPublisher;
        this.paymentStrategies = paymentStrategies;
    }

    public void process(ProcessPaymentDTO evento, String messageGroupId) {

        Optional<Payment> existing = transactionService.findByOrderId(String.valueOf(evento.orderId()));

        if (existing.isPresent()) {
            System.out.println("Idempotência: Pagamento já processado para a Ordem " + evento.orderId());
            eventPublisher.publishPaymentResult(existing.get(), messageGroupId);
            return;
        }

        Payment payment = transactionService.createPendingPayment(evento);

        PaymentStrategy strategy = paymentStrategies.get(payment.getPaymentMethod().name());
        if (strategy == null) {
            transactionService.updatePaymentStatus(payment.getId(), PaymentStatus.REJECTED);
            throw new IllegalArgumentException("Método de pagamento não encontrado.");
        }

        try {
            strategy.processPayment(payment);
            payment.setStatus(PaymentStatus.APPROVED);
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.REJECTED);
        }

        transactionService.updatePaymentStatus(payment.getId(), payment.getStatus());

        eventPublisher.publishPaymentResult(payment, messageGroupId);
    }
}