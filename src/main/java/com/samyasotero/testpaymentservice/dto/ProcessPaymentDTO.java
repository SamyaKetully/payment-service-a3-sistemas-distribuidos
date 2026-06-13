package com.samyasotero.testpaymentservice.dto;

import java.util.List;
import java.util.UUID;

public record ProcessPaymentDTO (
        UUID sagaId,
        UUID orderId,
        UUID userId,
        Integer installments,
        String paymentMethod,
        List<TicketEventDTO> tickets
) {}

