package com.example.cloudshare.repository;

import com.example.cloudshare.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByRazorpayOrderIdAndUserId(String razorpayOrderId, String userId);
}
