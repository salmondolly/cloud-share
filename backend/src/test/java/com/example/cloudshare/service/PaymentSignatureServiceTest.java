package com.example.cloudshare.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentSignatureServiceTest {
    private final PaymentSignatureService service = new PaymentSignatureService();

    @Test
    void verifiesExpectedHmacSignature() {
        String orderId = "order_test_123";
        String paymentId = "pay_test_456";
        String secret = "test-secret";
        String signature = service.hmacSha256(orderId + "|" + paymentId, secret);

        assertThat(service.verify(orderId, paymentId, signature, secret)).isTrue();
    }

    @Test
    void rejectsModifiedSignature() {
        assertThat(service.verify("order_1", "pay_1", "incorrect", "secret")).isFalse();
    }
}
