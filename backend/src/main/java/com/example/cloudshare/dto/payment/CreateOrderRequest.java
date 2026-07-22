package com.example.cloudshare.dto.payment;

import com.example.cloudshare.model.PlanType;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(@NotNull PlanType planType) {
}
