package com.example.cloudshare.controller;

import com.example.cloudshare.dto.payment.CreateOrderRequest;
import com.example.cloudshare.dto.payment.CreateOrderResponse;
import com.example.cloudshare.dto.payment.PaymentResponse;
import com.example.cloudshare.dto.payment.PlanResponse;
import com.example.cloudshare.dto.payment.VerifyPaymentRequest;
import com.example.cloudshare.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/plans")
    public List<PlanResponse> plans() {
        return paymentService.listPlans();
    }

    @PostMapping("/payments/create-order")
    public CreateOrderResponse createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return paymentService.createOrder(authentication.getName(), request.planType());
    }

    @PostMapping("/payments/verify")
    public PaymentResponse verify(
            Authentication authentication,
            @Valid @RequestBody VerifyPaymentRequest request
    ) {
        return paymentService.verifyPayment(
                authentication.getName(),
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );
    }
}
