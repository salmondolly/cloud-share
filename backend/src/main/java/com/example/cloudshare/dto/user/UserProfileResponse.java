package com.example.cloudshare.dto.user;

import com.example.cloudshare.model.PlanType;
import com.example.cloudshare.model.Role;

import java.time.Instant;

public record UserProfileResponse(
        String id,
        String name,
        String email,
        Role role,
        PlanType planType,
        int credits,
        long maxFileSizeBytes,
        Instant createdAt
) {
}
