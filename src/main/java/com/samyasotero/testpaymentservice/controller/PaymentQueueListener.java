package com.samyasotero.testpaymentservice.controller;

import com.samyasotero.testpaymentservice.dto.ProcessPaymentDTO;
import com.samyasotero.testpaymentservice.service.PaymentService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class PaymentQueueListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentQueueListener.class);

    private final PaymentService paymentService;

    public PaymentQueueListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @SqsListener("fila-processar-pagamento.fifo")
    public void escutarFilaProcessarPagamento(
            ProcessPaymentDTO evento,
            @Header("Sqs_Msa_MessageGroupId") String messageGroupId) {

        paymentService.process(evento, messageGroupId);
        System.out.println(">>>>>> O LISTENER FOI CHAMADO! <<<<<<");
        log.info("Iniciando processamento de requisicao de pagamento | orderId: {}", evento.orderId());

        try {
            paymentService.process(evento, messageGroupId);
        } catch (Exception e) {
            log.error("Falha inesperada no listener ao processar a mensagem | orderId: {}", evento.orderId(), e);
        }

    }
}
