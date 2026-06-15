package com.samyasotero.testpaymentservice.service;

import com.samyasotero.testpaymentservice.dto.ProcessPaymentDTO;
import com.samyasotero.testpaymentservice.dto.TicketEventDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import com.samyasotero.testpaymentservice.strategy.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

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

        long tempoInicio = System.currentTimeMillis();

        Optional<Payment> existing = transactionService.findByOrderId(evento.orderId());
        if (existing.isPresent()) {
            log.info("Processamento ignorado por idempotencia. Pagamento ja existente | orderId: {}", evento.orderId());
            return;
        }

        log.info("Iniciando processamento de pagamento | orderId: {} | paymentMethod: {}", evento.orderId(), evento.paymentMethod());

        PaymentStrategy strategy = paymentStrategies.get(evento.paymentMethod());
        if (strategy == null) {
            log.warn("Regra de negocio falhou: Metodo de pagamento invalido ou nao suportado | orderId: {} | paymentMethod: {}", evento.orderId(), evento.paymentMethod());
            throw new IllegalArgumentException("Método de pagamento não suportado: " + evento.paymentMethod());
        }

        Payment payment = transactionService.createPendingPayment(evento);

        try {
            List<TicketEventDTO> ticketsCalculados = strategy.processPayment(payment, evento.tickets(), evento.installments());

            transactionService.updateStatusAndAmount(payment.getId(), PaymentStatus.APPROVED, payment.getAmount(), payment.getProviderRef());

            payment.setStatus(PaymentStatus.APPROVED);
            payment.setAmount(ticketsCalculados.get(0).finalPrice());

            eventPublisher.publishPaymentResult(payment, ticketsCalculados, messageGroupId);

            long tempoExecucaoMs = System.currentTimeMillis() - tempoInicio;
            log.info("Pagamento aprovado com sucesso! Publicando evento na saga | orderId: {} | finalAmount: {} | tempoExecucaoMs: {}", payment.getOrderId(), payment.getAmount(), tempoExecucaoMs);

        } catch (Exception e) {
            log.error("Falha critica ao processar pagamento. Atualizando status para REJECTED | orderId: {} | errorMessage: {}", evento.orderId(), e.getMessage(), e);
            transactionService.updateStatusByOrderId(evento.orderId(), PaymentStatus.REJECTED);
        }
    }
}