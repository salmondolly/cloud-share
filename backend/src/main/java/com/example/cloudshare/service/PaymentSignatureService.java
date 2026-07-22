package com.example.cloudshare.service;

import com.example.cloudshare.exception.PaymentVerificationException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class PaymentSignatureService {
    public boolean verify(String orderId, String paymentId, String receivedSignature, String secret) {
        if (orderId == null || paymentId == null || receivedSignature == null || secret == null) {
            return false;
        }
        String payload = orderId + "|" + paymentId;
        String expected = hmacSha256(payload, secret);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                receivedSignature.getBytes(StandardCharsets.US_ASCII)
        );
    }

    String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new PaymentVerificationException("HMAC SHA-256 is not available on this JVM");
        } catch (java.security.InvalidKeyException exception) {
            throw new PaymentVerificationException("Payment signature key is invalid");
        }
    }
}
