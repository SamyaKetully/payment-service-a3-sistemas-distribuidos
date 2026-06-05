package com.samyasotero.testpaymentservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentDTO (
        UUID orderId,
        UUID seatId,
        UUID userId,
        BigDecimal amount,
        String paymentMethod
) {}

