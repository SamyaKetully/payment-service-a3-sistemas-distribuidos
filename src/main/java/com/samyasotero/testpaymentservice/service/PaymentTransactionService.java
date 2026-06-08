package com.samyasotero.testpaymentservice.service;

import com.samyasotero.testpaymentservice.dto.ProcessPaymentDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentMethod;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import com.samyasotero.testpaymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentTransactionService {

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
        return payment;
    }

    @Transactional
    public void updateStatusByOrderId(UUID orderId, PaymentStatus newStatus) {

        Payment payment = paymentRepository.findByOrderId(orderId);

        if (payment == null) {
            throw new RuntimeException("Pagamento não encontrado para o pedido: " + orderId);
        }

        payment.setStatus(newStatus);
        paymentRepository.update(payment);
    }

    @Transactional
    public void updateStatusAndAmount(UUID id, PaymentStatus status, BigDecimal amount, String providerRef) {

        Payment payment = paymentRepository.findById(id);

        if (payment == null) {
            throw new RuntimeException("Pagamento não encontrado para o ID: " + id);
        }

        payment.setStatus(status);
        payment.setAmount(amount);
        payment.setProviderRef(providerRef);

        paymentRepository.update(payment);

        System.out.println(" Banco atualizado! Pedido: " + payment.getOrderId() + " | Valor Final: R$ " + amount);
    }


}
