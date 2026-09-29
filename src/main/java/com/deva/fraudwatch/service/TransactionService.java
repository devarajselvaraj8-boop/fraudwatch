package com.deva.fraudwatch.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deva.fraudwatch.dto.TransactionRequest;
import com.deva.fraudwatch.dto.TransactionResponse;
import com.deva.fraudwatch.entity.FlaggedTransaction;
import com.deva.fraudwatch.entity.ReviewOutcome;
import com.deva.fraudwatch.entity.Rule;
import com.deva.fraudwatch.entity.Transaction;
import com.deva.fraudwatch.enums.ReviewStatus;
import com.deva.fraudwatch.enums.RuleType;
import com.deva.fraudwatch.enums.TransactionStatus;
import com.deva.fraudwatch.exception.BusinessRuleException;
import com.deva.fraudwatch.exception.ResourceNotFoundException;
import com.deva.fraudwatch.repository.FlaggedTransactionRepository;
import com.deva.fraudwatch.repository.ReviewOutcomeRepository;
import com.deva.fraudwatch.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final FlaggedTransactionRepository flaggedTransactionRepository;
    private final ReviewOutcomeRepository reviewOutcomeRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            FraudDetectionService fraudDetectionService,
            FlaggedTransactionRepository flaggedTransactionRepository,
            ReviewOutcomeRepository reviewOutcomeRepository) {

        this.transactionRepository = transactionRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.flaggedTransactionRepository = flaggedTransactionRepository;
        this.reviewOutcomeRepository = reviewOutcomeRepository;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        validateTransactionRequest(request);

        Transaction transaction = new Transaction();
        transaction.setSender(request.getSender());
        transaction.setReceiver(request.getReceiver());
        transaction.setAmount(request.getAmount());
        transaction.setDeleted(false);

        if (request.getTimestamp() == null) {
            transaction.setTimestamp(LocalDateTime.now());
        } else {
            transaction.setTimestamp(request.getTimestamp());
        }

        /*
         * Temporarily save the transaction first.
         * This allows the velocity rules to include
         * the transaction being evaluated.
         */
        transaction.setStatus(TransactionStatus.COMPLETED);
        Transaction saved = transactionRepository.save(transaction);

        // Run fraud detection across all active rules
        List<Rule> triggeredRules = fraudDetectionService.detectFraud(saved);

        // Count unique RuleTypes triggered
        Set<RuleType> uniqueTypes = triggeredRules.stream()
                .map(Rule::getType)
                .collect(Collectors.toSet());
        int uniqueCount = uniqueTypes.size();

        if (uniqueCount == 0) {
            // 0 unique rules: COMPLETED
            saved.setStatus(TransactionStatus.COMPLETED);
            saved = transactionRepository.save(saved);
        } else if (uniqueCount == 1) {
            // 1 unique rule: FLAGGED for manual review
            saved.setStatus(TransactionStatus.FLAGGED);
            saved = transactionRepository.save(saved);

            FlaggedTransaction flaggedTransaction = new FlaggedTransaction();
            flaggedTransaction.setTransaction(saved);
            flaggedTransaction.setTriggeredRules(new HashSet<>(triggeredRules));
            flaggedTransaction.setFlaggedAt(LocalDateTime.now());
            flaggedTransaction.setReviewStatus(ReviewStatus.PENDING);
            flaggedTransactionRepository.save(flaggedTransaction);
        } else {
            // 2 or more unique rules: BLOCKED automatically (no pending manual review)
            saved.setStatus(TransactionStatus.BLOCKED);
            saved = transactionRepository.save(saved);

            FlaggedTransaction flaggedTransaction = new FlaggedTransaction();
            flaggedTransaction.setTransaction(saved);
            flaggedTransaction.setTriggeredRules(new HashSet<>(triggeredRules));
            flaggedTransaction.setFlaggedAt(LocalDateTime.now());
            flaggedTransaction.setReviewStatus(ReviewStatus.BLOCKED);
            FlaggedTransaction savedFlagged = flaggedTransactionRepository.save(flaggedTransaction);

            ReviewOutcome outcome = new ReviewOutcome(
                    savedFlagged,
                    "SYSTEM",
                    "Automatically blocked: multiple fraud rules triggered ("
                    + uniqueTypes.stream().map(Enum::name).sorted().collect(Collectors.joining(", ")) + ")",
                    ReviewStatus.BLOCKED,
                    LocalDateTime.now()
            );
            reviewOutcomeRepository.save(outcome);
        }

        return new TransactionResponse(saved);
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findByDeletedFalse()
                .stream()
                .map(TransactionResponse::new)
                .toList();
    }

    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction with id " + id + " not found"));

        if (transaction.isDeleted()) {
            TransactionResponse response = new TransactionResponse();
            response.setMessage("Transaction with id " + id + " was previously deleted");
            return response;
        }

        return new TransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction with id " + id + " not found"));

        if (transaction.isDeleted()) {
            throw new BusinessRuleException("Transaction with id " + id + " was previously deleted and cannot be updated");
        }

        validateTransactionRequest(request);

        transaction.setSender(request.getSender());
        transaction.setReceiver(request.getReceiver());
        transaction.setAmount(request.getAmount());
        if (request.getTimestamp() != null) {
            transaction.setTimestamp(request.getTimestamp());
        }

        Optional<FlaggedTransaction> existingFlaggedOpt = flaggedTransactionRepository.findByTransactionId(id);

        Transaction saved = transactionRepository.save(transaction);
        List<Rule> triggeredRules = fraudDetectionService.detectFraud(saved);

        Set<RuleType> uniqueTypes = triggeredRules.stream()
                .map(Rule::getType)
                .collect(Collectors.toSet());
        int uniqueCount = uniqueTypes.size();

        if (uniqueCount == 0) {
            saved.setStatus(TransactionStatus.COMPLETED);
            if (existingFlaggedOpt.isPresent()) {
                FlaggedTransaction ft = existingFlaggedOpt.get();
                if (ft.getReviewStatus() == ReviewStatus.BLOCKED) {
                    saved.setStatus(TransactionStatus.BLOCKED);
                } else if (ft.getReviewStatus() == ReviewStatus.APPROVED) {
                    saved.setStatus(TransactionStatus.COMPLETED);
                } else {
                    saved.setStatus(TransactionStatus.COMPLETED);
                    ft.getTriggeredRules().clear();
                    flaggedTransactionRepository.save(ft);
                }
            }
        } else if (uniqueCount == 1) {
            if (existingFlaggedOpt.isPresent()) {
                FlaggedTransaction ft = existingFlaggedOpt.get();
                ft.setTriggeredRules(new HashSet<>(triggeredRules));
                flaggedTransactionRepository.save(ft);

                if (ft.getReviewStatus() == ReviewStatus.BLOCKED) {
                    saved.setStatus(TransactionStatus.BLOCKED);
                } else if (ft.getReviewStatus() == ReviewStatus.APPROVED) {
                    saved.setStatus(TransactionStatus.COMPLETED);
                } else {
                    saved.setStatus(TransactionStatus.FLAGGED);
                }
            } else {
                saved.setStatus(TransactionStatus.FLAGGED);
                saved = transactionRepository.save(saved);

                FlaggedTransaction ft = new FlaggedTransaction();
                ft.setTransaction(saved);
                ft.setTriggeredRules(new HashSet<>(triggeredRules));
                ft.setFlaggedAt(LocalDateTime.now());
                ft.setReviewStatus(ReviewStatus.PENDING);
                flaggedTransactionRepository.save(ft);
            }
        } else {
            // 2 or more unique rules: automatically BLOCKED
            saved.setStatus(TransactionStatus.BLOCKED);
            if (existingFlaggedOpt.isPresent()) {
                FlaggedTransaction ft = existingFlaggedOpt.get();
                ft.setTriggeredRules(new HashSet<>(triggeredRules));
                ft.setReviewStatus(ReviewStatus.BLOCKED);
                FlaggedTransaction savedFlagged = flaggedTransactionRepository.save(ft);

                Optional<ReviewOutcome> existingOutcome = reviewOutcomeRepository.findByFlaggedTransactionId(savedFlagged.getId());
                if (existingOutcome.isEmpty()) {
                    ReviewOutcome outcome = new ReviewOutcome(
                            savedFlagged,
                            "SYSTEM",
                            "Automatically blocked: multiple fraud rules triggered ("
                            + uniqueTypes.stream().map(Enum::name).sorted().collect(Collectors.joining(", ")) + ")",
                            ReviewStatus.BLOCKED,
                            LocalDateTime.now()
                    );
                    reviewOutcomeRepository.save(outcome);
                }
            } else {
                saved = transactionRepository.save(saved);
                FlaggedTransaction ft = new FlaggedTransaction();
                ft.setTransaction(saved);
                ft.setTriggeredRules(new HashSet<>(triggeredRules));
                ft.setFlaggedAt(LocalDateTime.now());
                ft.setReviewStatus(ReviewStatus.BLOCKED);
                FlaggedTransaction savedFlagged = flaggedTransactionRepository.save(ft);

                ReviewOutcome outcome = new ReviewOutcome(
                        savedFlagged,
                        "SYSTEM",
                        "Automatically blocked: multiple fraud rules triggered ("
                        + uniqueTypes.stream().map(Enum::name).sorted().collect(Collectors.joining(", ")) + ")",
                        ReviewStatus.BLOCKED,
                        LocalDateTime.now()
                );
                reviewOutcomeRepository.save(outcome);
            }
        }

        saved = transactionRepository.save(saved);
        return new TransactionResponse(saved);
    }

    @Transactional
    public TransactionResponse deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction with id " + id + " not found"));

        transaction.setDeleted(true);
        Transaction saved = transactionRepository.save(transaction);

        TransactionResponse response = new TransactionResponse(saved);
        response.setMessage("Transaction with id " + id + " was deleted successfully");
        return response;
    }

    private void validateTransactionRequest(TransactionRequest request) {
        if (request.getSender() == null || request.getSender().trim().isEmpty()) {
            throw new BusinessRuleException("Sender account is required");
        }
        if (request.getReceiver() == null || request.getReceiver().trim().isEmpty()) {
            throw new BusinessRuleException("Receiver account is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Amount must be positive");
        }
    }
}
