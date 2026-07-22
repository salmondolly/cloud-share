package com.example.cloudshare.service;

import com.example.cloudshare.dto.user.UserProfileResponse;
import com.example.cloudshare.exception.InsufficientCreditsException;
import com.example.cloudshare.exception.ResourceNotFoundException;
import com.example.cloudshare.model.PlanType;
import com.example.cloudshare.model.User;
import com.example.cloudshare.repository.UserRepository;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final PlanPolicyService planPolicyService;

    public UserService(
            UserRepository userRepository,
            MongoTemplate mongoTemplate,
            PlanPolicyService planPolicyService
    ) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
        this.planPolicyService = planPolicyService;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User consumeUploadCredit(String userId) {
        Query query = Query.query(Criteria.where("_id").is(userId).and("credits").gt(0));
        Update update = new Update().inc("credits", -1);
        User updated = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                User.class
        );
        if (updated == null) {
            throw new InsufficientCreditsException("No upload credits remaining. Upgrade your plan to continue.");
        }
        return updated;
    }

    public void refundUploadCredit(String userId) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(userId)),
                new Update().inc("credits", 1),
                User.class
        );
    }

    public User activatePlan(String userId, PlanType planType, String paymentId) {
        PlanPolicy policy = planPolicyService.getPolicy(planType);
        Query applyOnce = Query.query(Criteria.where("_id").is(userId)
                .and("processedPaymentIds").ne(paymentId));
        User updated = mongoTemplate.findAndModify(
                applyOnce,
                new Update()
                        .set("planType", planType)
                        .inc("credits", policy.uploadCredits())
                        .addToSet("processedPaymentIds", paymentId),
                FindAndModifyOptions.options().returnNew(true),
                User.class
        );
        if (updated != null) {
            return updated;
        }
        // A null result can also mean this payment was already applied; return the current user in that case.
        return findById(userId);
    }

    public UserProfileResponse toProfile(User user) {
        long maxFileSize = planPolicyService.getPolicy(user.getPlanType()).maxFileSizeBytes();
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPlanType(),
                user.getCredits(),
                maxFileSize,
                user.getCreatedAt()
        );
    }
}
