package com.samyasotero.testpaymentservice.strategy;

import com.samyasotero.testpaymentservice.dto.TicketEventDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component("BOLETO")
public class BoletoStrategy implements PaymentStrategy {

    private static final BigDecimal TAXA_CONVENIENCIA = new BigDecimal("0.10");
    @Override
    public List<TicketEventDTO> processPayment(Payment payment, List<TicketEventDTO> tickets, Integer installments) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<TicketEventDTO> processedTickets = new ArrayList<>();

        for (TicketEventDTO ticket : tickets) {

            BigDecimal valorConveniencia = ticket.ticketPrice().multiply(TAXA_CONVENIENCIA);
            BigDecimal precoFinal = ticket.ticketPrice().add(valorConveniencia).setScale(2, RoundingMode.HALF_UP);

            processedTickets.add(ticket.withFinalPrice(precoFinal));
            totalAmount = totalAmount.add(precoFinal);
        }

        payment.setAmount(totalAmount);

        payment.setProviderRef("BOL-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());

        return processedTickets;
    }
}