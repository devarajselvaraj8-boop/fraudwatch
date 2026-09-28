package com.deva.fraudwatch.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deva.fraudwatch.dto.ReviewRequest;
import com.deva.fraudwatch.dto.ReviewResponse;
import com.deva.fraudwatch.entity.FlaggedTransaction;
import com.deva.fraudwatch.entity.ReviewOutcome;
import com.deva.fraudwatch.entity.Transaction;
import com.deva.fraudwatch.enums.ReviewStatus;
import com.deva.fraudwatch.enums.TransactionStatus;
import com.deva.fraudwatch.exception.BusinessRuleException;
import com.deva.fraudwatch.exception.ResourceNotFoundException;
import com.deva.fraudwatch.repository.FlaggedTransactionRepository;
import com.deva.fraudwatch.repository.ReviewOutcomeRepository;
import com.deva.fraudwatch.repository.TransactionRepository;

@Service
public class ReviewService {

    private final FlaggedTransactionRepository flaggedTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final ReviewOutcomeRepository reviewOutcomeRepository;

    public ReviewService(
            FlaggedTransactionRepository flaggedTransactionRepository,
            TransactionRepository transactionRepository,
            ReviewOutcomeRepository reviewOutcomeRepository) {
        this.flaggedTransactionRepository = flaggedTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.reviewOutcomeRepository = reviewOutcomeRepository;
    }

    public List<ReviewResponse> getPendingReviews() {
        return flaggedTransactionRepository.findByReviewStatus(ReviewStatus.PENDING)
                .stream()
                .map(ReviewResponse::new)
                .toList();
    }

    public List<ReviewResponse> getAllReviews(ReviewStatus status) {
        List<FlaggedTransaction> list = (status != null)
                ? flaggedTransactionRepository.findByReviewStatus(status)
                : flaggedTransactionRepository.findAll();

        return list.stream()
                .map(ft -> {
                    ReviewOutcome outcome = reviewOutcomeRepository.findByFlaggedTransactionId(ft.getId()).orElse(null);
                    return new ReviewResponse(ft, outcome);
                })
                .toList();
    }

    public ReviewResponse getReviewById(Long id) {
        FlaggedTransaction flaggedTransaction = flaggedTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flagged transaction not found with id: " + id));

        ReviewOutcome outcome = reviewOutcomeRepository.findByFlaggedTransactionId(id).orElse(null);
        return new ReviewResponse(flaggedTransaction, outcome);
    }

    @Transactional
    public ReviewResponse approveReview(Long id, ReviewRequest request) {
        FlaggedTransaction flaggedTransaction = flaggedTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flagged transaction not found with id: " + id));

        if (flaggedTransaction.getReviewStatus() != ReviewStatus.PENDING) {
            throw new BusinessRuleException("Transaction has already been reviewed. Current status: " + flaggedTransaction.getReviewStatus());
        }

        flaggedTransaction.setReviewStatus(ReviewStatus.APPROVED);

        Transaction transaction = flaggedTransaction.getTransaction();
        transaction.setStatus(TransactionStatus.COMPLETED);

        transactionRepository.save(transaction);
        FlaggedTransaction savedFlagged = flaggedTransactionRepository.save(flaggedTransaction);

        ReviewOutcome outcome = new ReviewOutcome(
                savedFlagged,
                request.getReviewer(),
                request.getComment(),
                ReviewStatus.APPROVED,
                LocalDateTime.now()
        );
        ReviewOutcome savedOutcome = reviewOutcomeRepository.save(outcome);

        return new ReviewResponse(savedFlagged, savedOutcome);
    }

    @Transactional
    public ReviewResponse blockReview(Long id, ReviewRequest request) {
        FlaggedTransaction flaggedTransaction = flaggedTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flagged transaction not found with id: " + id));

        if (flaggedTransaction.getReviewStatus() != ReviewStatus.PENDING) {
            throw new BusinessRuleException("Transaction has already been reviewed. Current status: " + flaggedTransaction.getReviewStatus());
        }

        flaggedTransaction.setReviewStatus(ReviewStatus.BLOCKED);

        Transaction transaction = flaggedTransaction.getTransaction();
        transaction.setStatus(TransactionStatus.BLOCKED);

        transactionRepository.save(transaction);
        FlaggedTransaction savedFlagged = flaggedTransactionRepository.save(flaggedTransaction);

        ReviewOutcome outcome = new ReviewOutcome(
                savedFlagged,
                request.getReviewer(),
                request.getComment(),
                ReviewStatus.BLOCKED,
                LocalDateTime.now()
        );
        ReviewOutcome savedOutcome = reviewOutcomeRepository.save(outcome);

        return new ReviewResponse(savedFlagged, savedOutcome);
    }
}
