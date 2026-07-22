package com.example.cloudshare.dto.payment;

import com.example.cloudshare.model.PlanType;

public record PlanResponse(
        PlanType planType,
        String displayName,
        int uploadCredits,
        long maxFileSizeBytes,
        int pricePaise,
        String currency
) {
}
