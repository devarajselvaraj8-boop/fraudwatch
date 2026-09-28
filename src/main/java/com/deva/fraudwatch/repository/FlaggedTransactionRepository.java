package com.deva.fraudwatch.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.deva.fraudwatch.entity.FlaggedTransaction;
import com.deva.fraudwatch.enums.ReviewStatus;

@Repository
public interface FlaggedTransactionRepository extends JpaRepository<FlaggedTransaction, Long> {

    Optional<FlaggedTransaction> findByTransactionId(Long transactionId);

    List<FlaggedTransaction> findByReviewStatus(ReviewStatus reviewStatus);

    long countByReviewStatus(ReviewStatus reviewStatus);

    @Query("SELECT r.type, COUNT(r) FROM FlaggedTransaction ft JOIN ft.triggeredRules r GROUP BY r.type")
    List<Object[]> countTriggersByRuleType();
}
