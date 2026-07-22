package com.example.cloudshare.service;

import com.example.cloudshare.model.PlanType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanPolicyServiceTest {
    private final PlanPolicyService service = new PlanPolicyService(49900, 99900);

    @Test
    void returnsExpectedCreditsAndLimits() {
        assertThat(service.getPolicy(PlanType.FREE).uploadCredits()).isEqualTo(10);
        assertThat(service.getPolicy(PlanType.PREMIUM).uploadCredits()).isEqualTo(100);
        assertThat(service.getPolicy(PlanType.ULTIMATE).uploadCredits()).isEqualTo(500);
        assertThat(service.getPolicy(PlanType.ULTIMATE).maxFileSizeBytes())
                .isGreaterThan(service.getPolicy(PlanType.PREMIUM).maxFileSizeBytes());
    }
}
