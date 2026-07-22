package com.example.cloudshare.dto.auth;

import com.example.cloudshare.dto.user.UserProfileResponse;

public record AuthResponse(String token, String tokenType, UserProfileResponse user) {
}
