package com.samyasotero.testpaymentservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentDTO (
        UUID orderId,
        UUID userId,
        BigDecimal amount,
        String paymentMethod
) {}

