package com.deva.fraudwatch.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.deva.fraudwatch.dto.DashboardResponse;
import com.deva.fraudwatch.enums.ReviewStatus;
import com.deva.fraudwatch.enums.RuleType;
import com.deva.fraudwatch.enums.TransactionStatus;
import com.deva.fraudwatch.repository.FlaggedTransactionRepository;
import com.deva.fraudwatch.repository.TransactionRepository;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final FlaggedTransactionRepository flaggedTransactionRepository;

    public DashboardService(
            TransactionRepository transactionRepository,
            FlaggedTransactionRepository flaggedTransactionRepository) {
        this.transactionRepository = transactionRepository;
        this.flaggedTransactionRepository = flaggedTransactionRepository;
    }

    public DashboardResponse getDashboard() {
        long totalTransactions = transactionRepository.count();
        long completedTransactions = transactionRepository.countByStatus(TransactionStatus.COMPLETED);
        long flaggedTransactions = transactionRepository.countByStatus(TransactionStatus.FLAGGED);
        long blockedTransactions = transactionRepository.countByStatus(TransactionStatus.BLOCKED);
        long pendingReviews = flaggedTransactionRepository.countByReviewStatus(ReviewStatus.PENDING);
        long approvedReviews = flaggedTransactionRepository.countByReviewStatus(ReviewStatus.APPROVED);
        long blockedReviews = flaggedTransactionRepository.countByReviewStatus(ReviewStatus.BLOCKED);

        return new DashboardResponse(
                totalTransactions,
                completedTransactions,
                flaggedTransactions,
                blockedTransactions,
                pendingReviews,
                approvedReviews,
                blockedReviews
        );
    }

    public Map<String, Long> getRuleStatistics() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (RuleType type : RuleType.values()) {
            stats.put(type.name(), 0L);
        }

        List<Object[]> results = flaggedTransactionRepository.countTriggersByRuleType();
        for (Object[] result : results) {
            RuleType type = (RuleType) result[0];
            Long count = ((Number) result[1]).longValue();
            stats.put(type.name(), count);
        }

        return stats;
    }
}
