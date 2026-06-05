package com.samyasotero.testpaymentservice.controller;

import com.samyasotero.testpaymentservice.dto.ProcessPaymentDTO;
import com.samyasotero.testpaymentservice.service.PaymentService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class PaymentQueueListener {

    private final PaymentService paymentService;

    public PaymentQueueListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @SqsListener("fila-processar-pagamento.fifo")
    public void escutarFilaProcessarPagamento(
            ProcessPaymentDTO evento,
            @Header("Sqs_Msa_MessageGroupId") String messageGroupId) {
        paymentService.process(evento, messageGroupId);
    }
}
