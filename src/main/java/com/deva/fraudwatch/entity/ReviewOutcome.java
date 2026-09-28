package com.deva.fraudwatch.entity;

import java.time.LocalDateTime;

import com.deva.fraudwatch.enums.ReviewStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "review_outcome")
public class ReviewOutcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "flagged_transaction_id", nullable = false)
    private FlaggedTransaction flaggedTransaction;

    @NotBlank
    @Column(nullable = false)
    private String reviewer;

    private String comment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus outcome;

    @Column(nullable = false)
    private LocalDateTime reviewedAt;

    public ReviewOutcome() {
    }

    public ReviewOutcome(FlaggedTransaction flaggedTransaction, String reviewer, String comment, ReviewStatus outcome, LocalDateTime reviewedAt) {
        this.flaggedTransaction = flaggedTransaction;
        this.reviewer = reviewer;
        this.comment = comment;
        this.outcome = outcome;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FlaggedTransaction getFlaggedTransaction() {
        return flaggedTransaction;
    }

    public void setFlaggedTransaction(FlaggedTransaction flaggedTransaction) {
        this.flaggedTransaction = flaggedTransaction;
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

    public ReviewStatus getOutcome() {
        return outcome;
    }

    public void setOutcome(ReviewStatus outcome) {
        this.outcome = outcome;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
