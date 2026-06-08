package com.samyasotero.testpaymentservice.strategy;

import com.samyasotero.testpaymentservice.dto.TicketEventDTO;
import com.samyasotero.testpaymentservice.model.Payment;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component("CREDIT_CARD")
public class CreditCardStrategy implements PaymentStrategy {
    private static final BigDecimal TAXA_CONVENIENCIA = new BigDecimal("0.10");
    private static final BigDecimal JUROS_MENSAL = new BigDecimal("0.0349"); // 3.49%

    @Override
    public List<TicketEventDTO> processPayment(Payment payment, List<TicketEventDTO> tickets, Integer installments) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<TicketEventDTO> processedTickets = new ArrayList<>();

        int parcelas = (installments != null && installments > 0) ? installments : 1;

        for (TicketEventDTO ticket : tickets) {

            BigDecimal valorConveniencia = ticket.ticketPrice().multiply(TAXA_CONVENIENCIA);
            BigDecimal precoBase = ticket.ticketPrice().add(valorConveniencia);

            BigDecimal precoFinal = precoBase;

            if (parcelas >= 2 && parcelas <= 5) {
                BigDecimal fatorJuros = BigDecimal.ONE.add(JUROS_MENSAL).pow(parcelas);
                precoFinal = precoBase.multiply(fatorJuros);
            }

            precoFinal = precoFinal.setScale(2, RoundingMode.HALF_UP);
            processedTickets.add(ticket.withFinalPrice(precoFinal));
            totalAmount = totalAmount.add(precoFinal);
        }

        payment.setAmount(totalAmount);
        payment.setProviderRef("CC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return processedTickets;
    }
}