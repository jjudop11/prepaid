package com.prepaid.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentResponse {
    private String mId;
    private String version;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    private String requestedAt;
    private String approvedAt;
    private Long totalAmount;
    private Long balanceAmount;
    private String method;
}
