package com.samyasotero.testpaymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.NotNull;
import java.math.BigDecimal;

public class PaymentRequestDTO {

    private Long orderId;

    @NotNull
    private Long userId;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String paymentMethod;

    public PaymentRequestDTO() {
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
