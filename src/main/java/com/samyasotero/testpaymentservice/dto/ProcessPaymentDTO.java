package com.samyasotero.testpaymentservice.dto;

import java.util.List;
import java.util.UUID;

public record ProcessPaymentDTO (
        UUID sagaId,
        UUID orderId,
        Integer installments,
        String paymentMethod,
        List<TicketEventDTO> ticketList
) {}

