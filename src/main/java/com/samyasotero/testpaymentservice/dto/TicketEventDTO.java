package com.samyasotero.testpaymentservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketEventDTO(
    UUID ticketId,
    UUID orderId,
    UUID eventId,
    String seatIdentifier,
    String ticketType,
    BigDecimal ticketPrice,
    BigDecimal finalPrice
) {
    public TicketEventDTO withFinalPrice(BigDecimal calculatedPrice) {
        return new TicketEventDTO(ticketId, orderId, eventId, seatIdentifier, ticketType, ticketPrice, calculatedPrice);
    }
}
