package com.example.cloudshare.dto.payment;

import com.example.cloudshare.model.PaymentStatus;
import com.example.cloudshare.model.PlanType;

import java.time.Instant;

public record PaymentResponse(
        String id,
        String razorpayOrderId,
        String razorpayPaymentId,
        int amount,
        String currency,
        PlanType planType,
        PaymentStatus status,
        Instant createdAt
) {
}
