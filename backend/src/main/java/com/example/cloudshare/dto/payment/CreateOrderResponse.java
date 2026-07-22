package com.example.cloudshare.dto.payment;

import com.example.cloudshare.model.PlanType;

public record CreateOrderResponse(
        String orderId,
        String keyId,
        int amount,
        String currency,
        PlanType planType
) {
}
