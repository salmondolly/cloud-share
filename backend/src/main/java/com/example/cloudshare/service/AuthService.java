package com.example.cloudshare.service;

import com.example.cloudshare.dto.auth.AuthResponse;
import com.example.cloudshare.dto.auth.LoginRequest;
import com.example.cloudshare.dto.auth.RegisterRequest;
import com.example.cloudshare.exception.DuplicateResourceException;
import com.example.cloudshare.model.PlanType;
import com.example.cloudshare.model.Role;
import com.example.cloudshare.model.User;
import com.example.cloudshare.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserService userService;
    private final PlanPolicyService planPolicyService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService,
            UserService userService,
            PlanPolicyService planPolicyService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.planPolicyService = planPolicyService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setPlanType(PlanType.FREE);
        user.setCredits(planPolicyService.getPolicy(PlanType.FREE).uploadCredits());
        user.setCreatedAt(Instant.now());

        User saved = userRepository.save(user);
        UserDetails details = userDetailsService.loadUserByUsername(saved.getEmail());
        return new AuthResponse(jwtService.generateToken(details), "Bearer", userService.toProfile(saved));
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        User user = userService.findByEmail(email);
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        return new AuthResponse(jwtService.generateToken(details), "Bearer", userService.toProfile(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
