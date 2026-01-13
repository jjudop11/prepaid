package com.prepaid.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentConfirmRequest {
    @NotBlank
    private String paymentKey;

    @NotBlank
    private String orderId;

    @Min(1)
    private Long amount;
}
