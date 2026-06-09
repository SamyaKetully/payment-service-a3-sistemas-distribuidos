package com.samyasotero.testpaymentservice.service;

import com.samyasotero.testpaymentservice.dto.PaymentResultDTO;
import com.samyasotero.testpaymentservice.dto.TicketEventDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final SqsTemplate sqsTemplate;

    private static final String FILA_PEDIDO_SUCESSO = "fila-pedido-sucesso.fifo";
    private static final String FILA_PEDIDO_COMPENSADO = "fila-pedido-compensado.fifo";
    private static final String FILA_COMPENSAR_RESERVA = "fila-compensar-reserva.fifo";
    private static final String FILA_CONFIRMAR_RESERVA = "fila-confirmar-reserva.fifo";

    public PaymentEventPublisher(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
    }

    public void publishPaymentResult(Payment payment, List<TicketEventDTO> ticketsCalculados, String messageGroupId) {
        PaymentResultDTO resultado = new PaymentResultDTO(
                payment.getOrderId(),
                payment.getStatus().name()
        );

        log.info("Preparando envio de resultado para o OrderService | orderId: {} | status: {} | totalIngressos: {}",
                payment.getOrderId(), payment.getStatus(), ticketsCalculados.size());

        if (payment.getStatus() == PaymentStatus.APPROVED) {
            log.info("Iniciando fluxo de SUCESSO. Notificando Order e Seat Services | orderId: {}", payment.getOrderId());
            enviarParaFilaFifo(FILA_PEDIDO_SUCESSO, resultado, messageGroupId);
            enviarParaFilaFifo(FILA_CONFIRMAR_RESERVA, resultado, messageGroupId);

        } else if (payment.getStatus() == PaymentStatus.REJECTED || payment.getStatus() == PaymentStatus.CANCELED) {
            log.info("Iniciando fluxo de FALHA/COMPENSACAO (Saga). Notificando Order e Seat Services | orderId: {}", payment.getOrderId());
            enviarParaFilaFifo(FILA_PEDIDO_COMPENSADO, resultado, messageGroupId);
            enviarParaFilaFifo(FILA_COMPENSAR_RESERVA, resultado, messageGroupId);
        } else {
            log.warn("Tentativa de publicar evento na SAGA com status invalido ou PENDING | orderId: {} | status: {}",
                    payment.getOrderId(), payment.getStatus());
        }
    }

    private void enviarParaFilaFifo(String queueName, PaymentResultDTO payload, String messageGroupId) {
        try {
            sqsTemplate.send(to -> to
                    .queue(queueName)
                    .payload(payload)
                    .messageGroupId(messageGroupId)
            );
            log.info("Evento publicado com sucesso | queue: {} | orderId: {}", queueName, payload.orderId());
        } catch (Exception e) {
            log.error("Falha critica ao publicar evento na fila SQS | queue: {} | orderId: {} | errorMessage: {}",
                    queueName, payload.orderId(), e.getMessage(), e);
        }
    }
}