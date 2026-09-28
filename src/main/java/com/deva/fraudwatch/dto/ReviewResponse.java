package com.deva.fraudwatch.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.deva.fraudwatch.entity.FlaggedTransaction;
import com.deva.fraudwatch.entity.ReviewOutcome;
import com.deva.fraudwatch.enums.ReviewStatus;

public class ReviewResponse {

    private Long id;
    private TransactionResponse transaction;
    private Set<RuleResponse> triggeredRules;
    private LocalDateTime flaggedAt;
    private ReviewStatus reviewStatus;
    private String reviewer;
    private String comment;
    private LocalDateTime reviewedAt;

    public ReviewResponse() {
    }

    public ReviewResponse(FlaggedTransaction flaggedTransaction) {
        this(flaggedTransaction, null);
    }

    public ReviewResponse(FlaggedTransaction flaggedTransaction, ReviewOutcome outcome) {
        if (flaggedTransaction != null) {
            this.id = flaggedTransaction.getId();
            if (flaggedTransaction.getTransaction() != null) {
                this.transaction = new TransactionResponse(
                        flaggedTransaction.getTransaction().getId(),
                        flaggedTransaction.getTransaction().getSender(),
                        flaggedTransaction.getTransaction().getReceiver(),
                        flaggedTransaction.getTransaction().getAmount(),
                        flaggedTransaction.getTransaction().getTimestamp(),
                        flaggedTransaction.getTransaction().getStatus()
                );
            }
            if (flaggedTransaction.getTriggeredRules() != null) {
                this.triggeredRules = flaggedTransaction.getTriggeredRules().stream()
                        .map(RuleResponse::new)
                        .collect(Collectors.toSet());
            } else {
                this.triggeredRules = Collections.emptySet();
            }
            this.flaggedAt = flaggedTransaction.getFlaggedAt();
            this.reviewStatus = flaggedTransaction.getReviewStatus();
        }

        if (outcome != null) {
            this.reviewer = outcome.getReviewer();
            this.comment = outcome.getComment();
            this.reviewedAt = outcome.getReviewedAt();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionResponse getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionResponse transaction) {
        this.transaction = transaction;
    }

    public Set<RuleResponse> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(Set<RuleResponse> triggeredRules) {
        this.triggeredRules = triggeredRules;
    }

    public LocalDateTime getFlaggedAt() {
        return flaggedAt;
    }

    public void setFlaggedAt(LocalDateTime flaggedAt) {
        this.flaggedAt = flaggedAt;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
