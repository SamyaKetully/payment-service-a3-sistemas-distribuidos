package com.samyasotero.testpaymentservice.service;

import com.samyasotero.testpaymentservice.dto.ProcessPaymentDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentMethod;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import com.samyasotero.testpaymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentTransactionService {

    private static final Logger log = LoggerFactory.getLogger(PaymentTransactionService.class);
    private final PaymentRepository paymentRepository;

    public PaymentTransactionService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByOrderId(UUID orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId);
        return Optional.ofNullable(payment);

    }

    @Transactional
    public Payment createPendingPayment(ProcessPaymentDTO evento) {

        if (evento.paymentMethod() == null) {
            log.error("Metodo de pagamento nulo bloqueado na criacao | orderId: {}", evento.orderId());
            throw new IllegalArgumentException("O método de pagamento não pode ser nulo! Verifique o JSON da requisição.");
        }

        Payment payment = new Payment();

        payment.setId(com.fasterxml.uuid.Generators.timeBasedEpochGenerator().generate());

        payment.setOrderId(evento.orderId());
        payment.setUserId(evento.userId());
        payment.setAmount(java.math.BigDecimal.ZERO);

        payment.setPaymentMethod(PaymentMethod.valueOf(evento.paymentMethod()));
        payment.setStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);

        log.info("Novo pagamento PENDING registrado no banco de dados | orderId: {}", payment.getOrderId());

        return payment;
    }

    @Transactional
    public void updateStatusByOrderId(UUID orderId, PaymentStatus newStatus) {

        Payment payment = paymentRepository.findByOrderId(orderId);

        if (payment == null) {
            log.error("Tentativa de atualizar status de um pagamento inexistente | orderId: {}", orderId);
            throw new RuntimeException("Pagamento não encontrado para o pedido: " + orderId);
        }

        payment.setStatus(newStatus);
        paymentRepository.update(payment);

        log.info("Estado do pagamento atualizado no banco de dados | orderId: {} | newStatus: {}", orderId, newStatus);
    }

    @Transactional
    public void updateStatusAndAmount(UUID id, PaymentStatus status, BigDecimal amount, String providerRef) {

        Payment payment = paymentRepository.findById(id);

        if (payment == null) {
            log.error("Tentativa de atualizar valores de um pagamento inexistente | paymentId: {}", id);
            throw new RuntimeException("Pagamento não encontrado para o ID: " + id);
        }

        payment.setStatus(status);
        payment.setAmount(amount);
        payment.setProviderRef(providerRef);

        paymentRepository.update(payment);

        log.info("Pagamento consolidado no banco de dados | orderId: {} | newStatus: {} | amount: {}", payment.getOrderId(), status, amount);
    }


}
