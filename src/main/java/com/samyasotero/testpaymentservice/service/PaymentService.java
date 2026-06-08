package com.samyasotero.testpaymentservice.service;

import com.samyasotero.testpaymentservice.dto.ProcessPaymentDTO;
import com.samyasotero.testpaymentservice.dto.TicketEventDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import com.samyasotero.testpaymentservice.strategy.PaymentStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
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

        Optional<Payment> existing = transactionService.findByOrderId(evento.orderId());
        if (existing.isPresent()) {
            System.out.println("Idempotência: Pagamento já processado para a Ordem " + evento.orderId());
            return;
        }

        PaymentStrategy strategy = paymentStrategies.get(evento.paymentMethod());
        if (strategy == null) {
            throw new IllegalArgumentException("Método de pagamento não suportado: " + evento.paymentMethod());
        }

        Payment payment = transactionService.createPendingPayment(evento);

        try {
            List<TicketEventDTO> ticketsCalculados = strategy.processPayment(payment, evento.tickets(), evento.installments());

            transactionService.updateStatusAndAmount(payment.getId(), PaymentStatus.APPROVED, payment.getAmount(), payment.getProviderRef());

            eventPublisher.publishPaymentResult(payment, ticketsCalculados, messageGroupId);

        } catch (Exception e) {

            transactionService.updateStatusByOrderId(evento.orderId(), PaymentStatus.REJECTED);
            System.out.println("Erro ao processar pagamento: " + e.getMessage());
        }
    }
}