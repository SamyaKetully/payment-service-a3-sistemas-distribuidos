package com.samyasotero.testpaymentservice.service;

import com.fasterxml.uuid.Generators;
import com.samyasotero.testpaymentservice.dto.ProcessPaymentDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentMethod;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import com.samyasotero.testpaymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;

    public PaymentTransactionService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByOrderId(String orderId) {

        return paymentRepository.findByOrderId(orderId);
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
        payment.setAmount(evento.amount());

        payment.setPaymentMethod(PaymentMethod.valueOf(evento.paymentMethod()));
        payment.setStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);
        return payment;
    }

    @Transactional
    public void updatePaymentStatus(UUID paymentId, PaymentStatus status) {
        Optional<Payment> paymentOpt = Optional.ofNullable(paymentRepository.findById(paymentId));

        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(status);
            paymentRepository.update(payment);
        }
    }
}
