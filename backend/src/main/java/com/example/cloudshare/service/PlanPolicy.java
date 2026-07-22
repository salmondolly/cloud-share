package com.example.cloudshare.service;

public record PlanPolicy(
        String displayName,
        int uploadCredits,
        long maxFileSizeBytes,
        int pricePaise,
        String currency
) {
}
