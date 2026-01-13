package com.prepaid.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentUseRequest {
    @Min(1)
    private Long amount;

    @NotBlank
    private String merchantUid; // Service/Merchant ID consuming the balance
}
