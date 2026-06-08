package com.samyasotero.testpaymentservice.dto;

import java.util.UUID;

public record PaymentResultDTO (
        UUID orderId,
        String status)
{}
