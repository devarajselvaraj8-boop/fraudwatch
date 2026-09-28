package com.deva.fraudwatch.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.deva.fraudwatch.entity.Rule;
import com.deva.fraudwatch.entity.Transaction;
import com.deva.fraudwatch.enums.RuleType;
import com.deva.fraudwatch.repository.RuleRepository;
import com.deva.fraudwatch.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private FraudDetectionService fraudDetectionService;

    @BeforeEach
    void setUp() {
        fraudDetectionService = new FraudDetectionService(ruleRepository, transactionRepository);
    }

    @Test
    void testDetectFraud_HighAmountTriggered() {
        Rule highAmountRule = new Rule();
        highAmountRule.setId(1L);
        highAmountRule.setName("High Amount");
        highAmountRule.setType(RuleType.HIGH_AMOUNT);
        highAmountRule.setAmountThreshold(BigDecimal.valueOf(10000));
        highAmountRule.setActive(true);

        when(ruleRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of(highAmountRule));

        Transaction tx = new Transaction();
        tx.setSender("ACC001");
        tx.setReceiver("ACC002");
        tx.setAmount(BigDecimal.valueOf(15000));
        tx.setTimestamp(LocalDateTime.now());

        List<Rule> triggered = fraudDetectionService.detectFraud(tx);
        assertEquals(1, triggered.size());
        assertEquals("High Amount", triggered.get(0).getName());
    }

    @Test
    void testDetectFraud_NormalTransaction_NoRuleTriggered() {
        Rule highAmountRule = new Rule();
        highAmountRule.setId(1L);
        highAmountRule.setName("High Amount");
        highAmountRule.setType(RuleType.HIGH_AMOUNT);
        highAmountRule.setAmountThreshold(BigDecimal.valueOf(10000));
        highAmountRule.setActive(true);

        when(ruleRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of(highAmountRule));

        Transaction tx = new Transaction();
        tx.setSender("ACC001");
        tx.setReceiver("ACC002");
        tx.setAmount(BigDecimal.valueOf(5000));
        tx.setTimestamp(LocalDateTime.now());

        List<Rule> triggered = fraudDetectionService.detectFraud(tx);
        assertTrue(triggered.isEmpty());
    }

    @Test
    void testDetectFraud_VelocityTriggered() {
        Rule velocityRule = new Rule();
        velocityRule.setId(2L);
        velocityRule.setName("Velocity");
        velocityRule.setType(RuleType.VELOCITY);
        velocityRule.setTransactionCount(5);
        velocityRule.setTimeWindowMinutes(10);
        velocityRule.setActive(true);

        when(ruleRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of(velocityRule));
        when(transactionRepository.countRecentTransactions(eq("ACC500"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);

        Transaction tx = new Transaction();
        tx.setSender("ACC500");
        tx.setReceiver("ACC600");
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setTimestamp(LocalDateTime.now());

        List<Rule> triggered = fraudDetectionService.detectFraud(tx);
        assertEquals(1, triggered.size());
        assertEquals(RuleType.VELOCITY, triggered.get(0).getType());
    }

    @Test
    void testDetectFraud_BothRulesTriggered() {
        Rule highAmountRule = new Rule();
        highAmountRule.setId(1L);
        highAmountRule.setName("High Amount");
        highAmountRule.setType(RuleType.HIGH_AMOUNT);
        highAmountRule.setAmountThreshold(BigDecimal.valueOf(10000));
        highAmountRule.setActive(true);

        Rule velocityRule = new Rule();
        velocityRule.setId(2L);
        velocityRule.setName("Velocity");
        velocityRule.setType(RuleType.VELOCITY);
        velocityRule.setTransactionCount(5);
        velocityRule.setTimeWindowMinutes(10);
        velocityRule.setActive(true);

        when(ruleRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of(highAmountRule, velocityRule));
        when(transactionRepository.countRecentTransactions(eq("ACC500"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(6L);

        Transaction tx = new Transaction();
        tx.setSender("ACC500");
        tx.setReceiver("ACC600");
        tx.setAmount(BigDecimal.valueOf(20000));
        tx.setTimestamp(LocalDateTime.now());

        List<Rule> triggered = fraudDetectionService.detectFraud(tx);
        assertEquals(2, triggered.size());
    }

    @Test
    void testDetectFraud_DeletedRule_NotTriggered() {
        // Active non-deleted rule list is empty (because rule was soft-deleted)
        when(ruleRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of());

        Transaction tx = new Transaction();
        tx.setSender("ACC500");
        tx.setReceiver("ACC600");
        tx.setAmount(BigDecimal.valueOf(50000));
        tx.setTimestamp(LocalDateTime.now());

        List<Rule> triggered = fraudDetectionService.detectFraud(tx);
        assertTrue(triggered.isEmpty(), "Deleted rule must not trigger fraud detection");
    }
}
