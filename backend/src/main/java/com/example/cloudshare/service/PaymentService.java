package com.example.cloudshare.service;

import com.example.cloudshare.dto.payment.CreateOrderResponse;
import com.example.cloudshare.dto.payment.PaymentResponse;
import com.example.cloudshare.exception.PaymentException;
import com.example.cloudshare.exception.PaymentVerificationException;
import com.example.cloudshare.exception.ResourceNotFoundException;
import com.example.cloudshare.model.Payment;
import com.example.cloudshare.model.PaymentStatus;
import com.example.cloudshare.model.PlanType;
import com.example.cloudshare.model.User;
import com.example.cloudshare.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final PlanPolicyService planPolicyService;
    private final PaymentSignatureService signatureService;
    private final String keyId;
    private final String keySecret;

    public PaymentService(
            PaymentRepository paymentRepository,
            UserService userService,
            PlanPolicyService planPolicyService,
            PaymentSignatureService signatureService,
            @Value("${app.razorpay.key-id:}") String keyId,
            @Value("${app.razorpay.key-secret:}") String keySecret
    ) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.planPolicyService = planPolicyService;
        this.signatureService = signatureService;
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    public CreateOrderResponse createOrder(String email, PlanType planType) {
        requireConfiguredCredentials();
        if (planType == PlanType.FREE) {
            throw new PaymentVerificationException("The Free plan does not require payment");
        }

        User user = userService.findByEmail(email);
        PlanPolicy policy = planPolicyService.getPolicy(planType);
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount", policy.pricePaise());
            options.put("currency", policy.currency());
            options.put("receipt", "cloudshare_" + UUID.randomUUID().toString().substring(0, 18));
            options.put("payment_capture", 1);
            JSONObject notes = new JSONObject();
            notes.put("userId", user.getId());
            notes.put("planType", planType.name());
            options.put("notes", notes);

            Order order = client.orders.create(options);
            String orderId = order.get("id");

            Payment payment = new Payment();
            payment.setUserId(user.getId());
            payment.setRazorpayOrderId(orderId);
            payment.setAmount(policy.pricePaise());
            payment.setCurrency(policy.currency());
            payment.setPlanType(planType);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setCreatedAt(Instant.now());
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);

            return new CreateOrderResponse(orderId, keyId, policy.pricePaise(), policy.currency(), planType);
        } catch (RazorpayException exception) {
            throw new PaymentException("Razorpay order creation failed. Check your test credentials and network.", exception);
        }
    }

    public synchronized PaymentResponse verifyPayment(
            String email,
            String orderId,
            String paymentId,
            String signature
    ) {
        requireConfiguredCredentials();
        User user = userService.findByEmail(email);
        Payment payment = paymentRepository.findByRazorpayOrderIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (paymentId.equals(payment.getRazorpayPaymentId())) {
                userService.activatePlan(user.getId(), payment.getPlanType(), paymentId);
                return toResponse(payment);
            }
            throw new PaymentVerificationException("This order has already been verified with another payment");
        }

        boolean valid = signatureService.verify(
                payment.getRazorpayOrderId(),
                paymentId,
                signature,
                keySecret
        );
        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);
            throw new PaymentVerificationException("Razorpay payment signature verification failed");
        }

        payment.setRazorpayPaymentId(paymentId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setUpdatedAt(Instant.now());
        Payment saved = paymentRepository.save(payment);
        userService.activatePlan(user.getId(), payment.getPlanType(), paymentId);
        return toResponse(saved);
    }

    public List<com.example.cloudshare.dto.payment.PlanResponse> listPlans() {
        return planPolicyService.listPlans();
    }

    private void requireConfiguredCredentials() {
        if (!StringUtils.hasText(keyId) || !StringUtils.hasText(keySecret)) {
            throw new PaymentException("Razorpay is not configured. Add RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getRazorpayOrderId(),
                payment.getRazorpayPaymentId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPlanType(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }
}
