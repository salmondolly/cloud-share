package com.example.cloudshare.service;

import com.example.cloudshare.dto.payment.PlanResponse;
import com.example.cloudshare.model.PlanType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanPolicyService {
    private static final long MEBIBYTE = 1024L * 1024L;

    private final Map<PlanType, PlanPolicy> policies = new EnumMap<>(PlanType.class);

    public PlanPolicyService(
            @Value("${app.plans.premium-price-paise:49900}") int premiumPricePaise,
            @Value("${app.plans.ultimate-price-paise:99900}") int ultimatePricePaise
    ) {
        policies.put(PlanType.FREE, new PlanPolicy("Free", 10, 10 * MEBIBYTE, 0, "INR"));
        policies.put(PlanType.PREMIUM, new PlanPolicy("Premium", 100, 100 * MEBIBYTE, premiumPricePaise, "INR"));
        policies.put(PlanType.ULTIMATE, new PlanPolicy("Ultimate", 500, 500 * MEBIBYTE, ultimatePricePaise, "INR"));
    }

    public PlanPolicy getPolicy(PlanType planType) {
        PlanPolicy policy = policies.get(planType);
        if (policy == null) {
            throw new IllegalArgumentException("Unsupported plan: " + planType);
        }
        return policy;
    }

    public List<PlanResponse> listPlans() {
        return Arrays.stream(PlanType.values())
                .map(type -> {
                    PlanPolicy policy = getPolicy(type);
                    return new PlanResponse(
                            type,
                            policy.displayName(),
                            policy.uploadCredits(),
                            policy.maxFileSizeBytes(),
                            policy.pricePaise(),
                            policy.currency()
                    );
                })
                .toList();
    }
}
