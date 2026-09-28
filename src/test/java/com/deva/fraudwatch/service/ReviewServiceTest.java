package com.deva.fraudwatch.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private FlaggedTransactionRepository flaggedTransactionRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ReviewOutcomeRepository reviewOutcomeRepository;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(flaggedTransactionRepository, transactionRepository, reviewOutcomeRepository);
    }

    @Test
    void testApproveReview_Success() {
        Transaction tx = new Transaction();
        tx.setId(10L);
        tx.setStatus(TransactionStatus.FLAGGED);

        FlaggedTransaction flaggedTx = new FlaggedTransaction();
        flaggedTx.setId(1L);
        flaggedTx.setTransaction(tx);
        flaggedTx.setReviewStatus(ReviewStatus.PENDING);
        flaggedTx.setFlaggedAt(LocalDateTime.now());

        when(flaggedTransactionRepository.findById(1L)).thenReturn(Optional.of(flaggedTx));
        when(flaggedTransactionRepository.save(any(FlaggedTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(reviewOutcomeRepository.save(any(ReviewOutcome.class))).thenAnswer(i -> i.getArgument(0));

        ReviewRequest request = new ReviewRequest("admin", "Approved after verification");
        ReviewResponse response = reviewService.approveReview(1L, request);

        assertNotNull(response);
        assertEquals(ReviewStatus.APPROVED, response.getReviewStatus());
        assertEquals(TransactionStatus.COMPLETED, tx.getStatus());
        verify(transactionRepository).save(tx);
        verify(flaggedTransactionRepository).save(flaggedTx);
        verify(reviewOutcomeRepository).save(any(ReviewOutcome.class));
    }

    @Test
    void testBlockReview_Success() {
        Transaction tx = new Transaction();
        tx.setId(10L);
        tx.setStatus(TransactionStatus.FLAGGED);

        FlaggedTransaction flaggedTx = new FlaggedTransaction();
        flaggedTx.setId(1L);
        flaggedTx.setTransaction(tx);
        flaggedTx.setReviewStatus(ReviewStatus.PENDING);
        flaggedTx.setFlaggedAt(LocalDateTime.now());

        when(flaggedTransactionRepository.findById(1L)).thenReturn(Optional.of(flaggedTx));
        when(flaggedTransactionRepository.save(any(FlaggedTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(reviewOutcomeRepository.save(any(ReviewOutcome.class))).thenAnswer(i -> i.getArgument(0));

        ReviewRequest request = new ReviewRequest("admin", "Blocked as confirmed fraud");
        ReviewResponse response = reviewService.blockReview(1L, request);

        assertNotNull(response);
        assertEquals(ReviewStatus.BLOCKED, response.getReviewStatus());
        assertEquals(TransactionStatus.BLOCKED, tx.getStatus());
        verify(transactionRepository).save(tx);
        verify(flaggedTransactionRepository).save(flaggedTx);
        verify(reviewOutcomeRepository).save(any(ReviewOutcome.class));
    }

    @Test
    void testApproveReview_AlreadyReviewed_ThrowsBusinessRuleException() {
        Transaction tx = new Transaction();
        tx.setId(10L);
        tx.setStatus(TransactionStatus.COMPLETED);

        FlaggedTransaction flaggedTx = new FlaggedTransaction();
        flaggedTx.setId(1L);
        flaggedTx.setTransaction(tx);
        flaggedTx.setReviewStatus(ReviewStatus.APPROVED);

        when(flaggedTransactionRepository.findById(1L)).thenReturn(Optional.of(flaggedTx));

        ReviewRequest request = new ReviewRequest("admin", "Duplicate review");
        assertThrows(BusinessRuleException.class, () -> reviewService.approveReview(1L, request));
    }

    @Test
    void testGetReviewById_NotFound_ThrowsResourceNotFoundException() {
        when(flaggedTransactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(99L));
    }
}
