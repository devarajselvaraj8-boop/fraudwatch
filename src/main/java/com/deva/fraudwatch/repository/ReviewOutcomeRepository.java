package com.deva.fraudwatch.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.deva.fraudwatch.entity.FlaggedTransaction;
import com.deva.fraudwatch.entity.ReviewOutcome;

@Repository
public interface ReviewOutcomeRepository extends JpaRepository<ReviewOutcome, Long> {

    Optional<ReviewOutcome> findByFlaggedTransaction(FlaggedTransaction flaggedTransaction);

    Optional<ReviewOutcome> findByFlaggedTransactionId(Long flaggedTransactionId);
}
