package com.samyasotero.testpaymentservice.strategy;

import com.samyasotero.testpaymentservice.dto.TicketEventDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component("PIX")
public class PixStrategy implements PaymentStrategy {
    @Override
    public List<TicketEventDTO> processPayment(Payment payment, List<TicketEventDTO> tickets, Integer installments) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<TicketEventDTO> processedTickets = new ArrayList<>();

        for (TicketEventDTO ticket : tickets) {

            BigDecimal precoFinal = ticket.ticketPrice();

            processedTickets.add(ticket.withFinalPrice(precoFinal));
            totalAmount = totalAmount.add(precoFinal);
        }

        payment.setAmount(totalAmount);
        payment.setProviderRef("PIX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return processedTickets;
    }
}