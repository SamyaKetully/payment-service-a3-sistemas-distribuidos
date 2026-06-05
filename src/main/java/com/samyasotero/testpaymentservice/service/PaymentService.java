package com.samyasotero.testpaymentservice.service;

import com.fasterxml.uuid.Generators;
import com.samyasotero.testpaymentservice.dto.PaymentResultDTO;
import com.samyasotero.testpaymentservice.dto.ProcessPaymentDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentMethod;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import com.samyasotero.testpaymentservice.repository.PaymentRepository;
import com.samyasotero.testpaymentservice.strategy.PaymentStrategy;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Map<String, PaymentStrategy> paymentStrategies;
    private final SqsTemplate sqsTemplate;

    private static final String FILA_PEDIDO_SUCESSO = "fila-pedido-sucesso.fifo";
    private static final String FILA_PEDIDO_COMPENSADO = "fila-pedido-compensado.fifo";
    private static final String FILA_COMPENSAR_RESERVA = "fila-compensar-reserva.fifo";
    private static final String FILA_CONFIRMAR_RESERVA = "fila-confirmar-reserva.fifo";

    public PaymentService(PaymentRepository paymentRepository, Map<String, PaymentStrategy> paymentStrategies, SqsTemplate sqsTemplate) {
        this.paymentRepository = paymentRepository;
        this.paymentStrategies = paymentStrategies;
        this.sqsTemplate = sqsTemplate;
    }

    @Transactional
    public void process(ProcessPaymentDTO evento, String messageGroupId) {

        Payment payment = new Payment();
        UUID paymentUuid = Generators.timeBasedEpochGenerator().generate();
        payment.setId(paymentUuid);
        payment.setOrderId(evento.orderId());
        payment.setUserId(evento.userId());
        payment.setAmount(evento.amount());
        payment.setPaymentMethod(PaymentMethod.valueOf(evento.paymentMethod()));
        payment.setStatus(PaymentStatus.PENDING);

        PaymentStrategy strategy = paymentStrategies.get(payment.getPaymentMethod().name());
        if (strategy == null) {
            throw new IllegalArgumentException("Método de pagamento não encontrado.");
        }

        strategy.processPayment(payment);
        paymentRepository.save(payment);
        payment.setStatus(PaymentStatus.APPROVED);

        PaymentResultDTO resultado = new PaymentResultDTO(
            payment.getOrderId(),
                evento.seatId(),
            payment.getStatus().name()
        );

        if (payment.getStatus() == PaymentStatus.APPROVED) {

            System.out.println("Pagamento Aprovado! Enviando sucesso para Order e Seat Services.");
            enviarParaFilaFifo(FILA_PEDIDO_SUCESSO, resultado, messageGroupId);
            enviarParaFilaFifo(FILA_CONFIRMAR_RESERVA, resultado, messageGroupId);

            paymentRepository.update(payment);

        } else if (payment.getStatus() == PaymentStatus.REJECTED || payment.getStatus() == PaymentStatus.CANCELLED) {

            System.out.println("Pagamento Falhou! Iniciando Saga de Compensação em paralelo.");
            enviarParaFilaFifo(FILA_PEDIDO_COMPENSADO, resultado, messageGroupId);
            enviarParaFilaFifo(FILA_COMPENSAR_RESERVA, resultado, messageGroupId);

            paymentRepository.update(payment);
        }

    }

    private void enviarParaFilaFifo(String queueName, PaymentResultDTO payload, String messageGroupId) {
        sqsTemplate.send(to -> to
                .queue(queueName)
                .payload(payload)
                .messageGroupId(messageGroupId)
        );
    }
}
