package com.deva.fraudwatch.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testDetectFraud_SenderVelocityTriggered() {
        Rule velocityRule = new Rule();
        velocityRule.setId(2L);
        velocityRule.setName("Sender Velocity");
        velocityRule.setType(RuleType.SENDER_VELOCITY);
        velocityRule.setTransactionCount(5);
        velocityRule.setTimeWindowMinutes(10);
        velocityRule.setActive(true);

        when(ruleRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of(velocityRule));
        when(transactionRepository.countRecentTransactionsBySender(eq("ACC500"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);

        Transaction tx = new Transaction();
        tx.setSender("ACC500");
        tx.setReceiver("ACC600");
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setTimestamp(LocalDateTime.now());

        List<Rule> triggered = fraudDetectionService.detectFraud(tx);
        assertEquals(1, triggered.size());
        assertEquals(RuleType.SENDER_VELOCITY, triggered.get(0).getType());
    }

    @Test
    void testDetectFraud_ReceiverVelocityTriggered() {
        Rule velocityRule = new Rule();
        velocityRule.setId(3L);
        velocityRule.setName("Receiver Velocity");
        velocityRule.setType(RuleType.RECEIVER_VELOCITY);
        velocityRule.setTransactionCount(5);
        velocityRule.setTimeWindowMinutes(10);
        velocityRule.setActive(true);

        when(ruleRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of(velocityRule));
        when(transactionRepository.countRecentTransactionsByReceiver(eq("ACC600"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);

        Transaction tx = new Transaction();
        tx.setSender("ACC500");
        tx.setReceiver("ACC600");
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setTimestamp(LocalDateTime.now());

        List<Rule> triggered = fraudDetectionService.detectFraud(tx);
        assertEquals(1, triggered.size());
        assertEquals(RuleType.RECEIVER_VELOCITY, triggered.get(0).getType());
    }

    @Test
    void testDetectFraud_AllThreeRulesTriggered() {
        Rule highAmountRule = new Rule();
        highAmountRule.setId(1L);
        highAmountRule.setName("High Amount");
        highAmountRule.setType(RuleType.HIGH_AMOUNT);
        highAmountRule.setAmountThreshold(BigDecimal.valueOf(10000));
        highAmountRule.setActive(true);

        Rule senderVelocityRule = new Rule();
        senderVelocityRule.setId(2L);
        senderVelocityRule.setName("Sender Velocity");
        senderVelocityRule.setType(RuleType.SENDER_VELOCITY);
        senderVelocityRule.setTransactionCount(5);
        senderVelocityRule.setTimeWindowMinutes(10);
        senderVelocityRule.setActive(true);

        Rule receiverVelocityRule = new Rule();
        receiverVelocityRule.setId(3L);
        receiverVelocityRule.setName("Receiver Velocity");
        receiverVelocityRule.setType(RuleType.RECEIVER_VELOCITY);
        receiverVelocityRule.setTransactionCount(5);
        receiverVelocityRule.setTimeWindowMinutes(10);
        receiverVelocityRule.setActive(true);

        when(ruleRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of(highAmountRule, senderVelocityRule, receiverVelocityRule));
        when(transactionRepository.countRecentTransactionsBySender(eq("ACC500"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(6L);
        when(transactionRepository.countRecentTransactionsByReceiver(eq("ACC600"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(6L);

        Transaction tx = new Transaction();
        tx.setSender("ACC500");
        tx.setReceiver("ACC600");
        tx.setAmount(BigDecimal.valueOf(20000));
        tx.setTimestamp(LocalDateTime.now());

        List<Rule> triggered = fraudDetectionService.detectFraud(tx);
        assertEquals(3, triggered.size());
    }

    @Test
    void testDetectFraud_DeletedRule_NotTriggered() {
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
