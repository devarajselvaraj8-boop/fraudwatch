package com.deva.fraudwatch.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.deva.fraudwatch.enums.ReviewStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "flagged_transactions")
public class FlaggedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToMany
    @JoinTable(
            name = "flagged_transaction_rules",
            joinColumns = @JoinColumn(name = "flagged_transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "rule_id")
    )
    private Set<Rule> triggeredRules = new HashSet<>();

    private LocalDateTime flaggedAt;

    @Enumerated(EnumType.STRING)
    private ReviewStatus reviewStatus;

    public FlaggedTransaction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Set<Rule> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(Set<Rule> triggeredRules) {
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
}
