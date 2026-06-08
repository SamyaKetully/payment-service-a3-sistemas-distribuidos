package com.samyasotero.testpaymentservice.service;

import com.samyasotero.testpaymentservice.dto.PaymentResultDTO;
import com.samyasotero.testpaymentservice.dto.TicketEventDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import com.samyasotero.testpaymentservice.model.enums.PaymentStatus;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentEventPublisher {

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

        System.out.println("Enviando resultado para o OrderService...");
        System.out.println("Pedido: " + payment.getOrderId());
        System.out.println("Status: " + payment.getStatus());
        System.out.println("Total Ingressos Processados: " + ticketsCalculados.size());


        if (payment.getStatus() == PaymentStatus.APPROVED) {
            System.out.println("Pagamento Aprovado! Enviando sucesso para Order e Seat Services.");
            enviarParaFilaFifo(FILA_PEDIDO_SUCESSO, resultado, messageGroupId);
            enviarParaFilaFifo(FILA_CONFIRMAR_RESERVA, resultado, messageGroupId);

        } else if (payment.getStatus() == PaymentStatus.REJECTED || payment.getStatus() == PaymentStatus.CANCELED) {
            System.out.println("Pagamento Falhou! Iniciando Saga de Compensação em paralelo.");
            enviarParaFilaFifo(FILA_PEDIDO_COMPENSADO, resultado, messageGroupId);
            enviarParaFilaFifo(FILA_COMPENSAR_RESERVA, resultado, messageGroupId);
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
