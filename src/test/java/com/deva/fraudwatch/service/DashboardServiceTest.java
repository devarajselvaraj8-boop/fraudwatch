package com.deva.fraudwatch.service;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.deva.fraudwatch.dto.DashboardResponse;
import com.deva.fraudwatch.enums.ReviewStatus;
import com.deva.fraudwatch.enums.RuleType;
import com.deva.fraudwatch.enums.TransactionStatus;
import com.deva.fraudwatch.repository.FlaggedTransactionRepository;
import com.deva.fraudwatch.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FlaggedTransactionRepository flaggedTransactionRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(transactionRepository, flaggedTransactionRepository);
    }

    @Test
    void testGetDashboard() {
        when(transactionRepository.count()).thenReturn(10L);
        when(transactionRepository.countByStatus(TransactionStatus.COMPLETED)).thenReturn(6L);
        when(transactionRepository.countByStatus(TransactionStatus.FLAGGED)).thenReturn(3L);
        when(transactionRepository.countByStatus(TransactionStatus.BLOCKED)).thenReturn(1L);
        when(flaggedTransactionRepository.countByReviewStatus(ReviewStatus.PENDING)).thenReturn(2L);
        when(flaggedTransactionRepository.countByReviewStatus(ReviewStatus.APPROVED)).thenReturn(1L);
        when(flaggedTransactionRepository.countByReviewStatus(ReviewStatus.BLOCKED)).thenReturn(1L);

        DashboardResponse response = dashboardService.getDashboard();

        assertNotNull(response);
        assertEquals(10L, response.getTotalTransactions());
        assertEquals(6L, response.getCompletedTransactions());
        assertEquals(3L, response.getFlaggedTransactions());
        assertEquals(1L, response.getBlockedTransactions());
        assertEquals(2L, response.getPendingReviews());
        assertEquals(1L, response.getApprovedReviews());
        assertEquals(1L, response.getBlockedReviews());
    }

    @Test
    void testGetRuleStatistics() {
        List<Object[]> rows = List.of(
                new Object[]{RuleType.HIGH_AMOUNT, 8L},
                new Object[]{RuleType.VELOCITY, 4L}
        );
        when(flaggedTransactionRepository.countTriggersByRuleType()).thenReturn(rows);

        Map<String, Long> stats = dashboardService.getRuleStatistics();

        assertNotNull(stats);
        assertEquals(8L, stats.get("HIGH_AMOUNT"));
        assertEquals(4L, stats.get("VELOCITY"));
    }
}
