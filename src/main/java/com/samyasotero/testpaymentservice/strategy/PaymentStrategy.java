package com.samyasotero.testpaymentservice.strategy;
import com.samyasotero.testpaymentservice.dto.TicketEventDTO;
import com.samyasotero.testpaymentservice.model.Payment;

import java.util.List;

public interface PaymentStrategy {

    List<TicketEventDTO> processPayment(Payment payment, List<TicketEventDTO> tickets, Integer installments);

}
