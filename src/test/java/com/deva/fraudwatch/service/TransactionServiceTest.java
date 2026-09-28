package com.deva.fraudwatch.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.deva.fraudwatch.dto.TransactionRequest;
import com.deva.fraudwatch.dto.TransactionResponse;
import com.deva.fraudwatch.entity.FlaggedTransaction;
import com.deva.fraudwatch.entity.Rule;
import com.deva.fraudwatch.entity.Transaction;
import com.deva.fraudwatch.enums.ReviewStatus;
import com.deva.fraudwatch.enums.RuleType;
import com.deva.fraudwatch.enums.TransactionStatus;
import com.deva.fraudwatch.exception.BusinessRuleException;
import com.deva.fraudwatch.exception.ResourceNotFoundException;
import com.deva.fraudwatch.repository.FlaggedTransactionRepository;
import com.deva.fraudwatch.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private FlaggedTransactionRepository flaggedTransactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository,
                fraudDetectionService,
                flaggedTransactionRepository
        );
    }

    @Test
    void testCreateTransaction_Success_Completed() {
        TransactionRequest req = new TransactionRequest();
        req.setSender("ACC100");
        req.setReceiver("ACC200");
        req.setAmount(BigDecimal.valueOf(500));

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(10L);
            return t;
        });
        when(fraudDetectionService.detectFraud(any(Transaction.class))).thenReturn(List.of());

        TransactionResponse res = transactionService.createTransaction(req);
        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals(TransactionStatus.COMPLETED, res.getStatus());
        assertFalse(res.isDeleted());
    }

    @Test
    void testCreateTransaction_TriggeredFraud_Flagged() {
        TransactionRequest req = new TransactionRequest();
        req.setSender("ACC100");
        req.setReceiver("ACC200");
        req.setAmount(BigDecimal.valueOf(25000));

        Rule rule = new Rule();
        rule.setId(1L);
        rule.setName("High Amount");
        rule.setType(RuleType.HIGH_AMOUNT);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(11L);
            return t;
        });
        when(fraudDetectionService.detectFraud(any(Transaction.class))).thenReturn(List.of(rule));

        TransactionResponse res = transactionService.createTransaction(req);
        assertNotNull(res);
        assertEquals(TransactionStatus.FLAGGED, res.getStatus());
        verify(flaggedTransactionRepository).save(any(FlaggedTransaction.class));
    }

    @Test
    void testGetAllTransactions_ReturnsOnlyNonDeleted() {
        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setSender("ACC1");
        t1.setReceiver("ACC2");
        t1.setAmount(BigDecimal.valueOf(100));
        t1.setDeleted(false);

        Transaction t2 = new Transaction();
        t2.setId(2L);
        t2.setSender("ACC3");
        t2.setReceiver("ACC4");
        t2.setAmount(BigDecimal.valueOf(200));
        t2.setDeleted(false);

        when(transactionRepository.findByDeletedFalse()).thenReturn(List.of(t1, t2));

        List<TransactionResponse> list = transactionService.getAllTransactions();
        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(2L, list.get(1).getId());
    }

    @Test
    void testGetTransactionById_Deleted_ReturnsPreviouslyDeletedMessage() {
        Transaction tx = new Transaction();
        tx.setId(5L);
        tx.setSender("ACC1");
        tx.setReceiver("ACC2");
        tx.setAmount(BigDecimal.valueOf(500));
        tx.setDeleted(true);

        when(transactionRepository.findById(5L)).thenReturn(Optional.of(tx));

        TransactionResponse res = transactionService.getTransactionById(5L);
        assertNotNull(res);
        assertEquals("Transaction with id 5 was previously deleted", res.getMessage());
    }

    @Test
    void testGetTransactionById_NotFound_Throws404() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(999L));
        assertEquals("Transaction with id 999 not found", ex.getMessage());
    }

    @Test
    void testUpdateTransaction_Success() {
        Transaction tx = new Transaction();
        tx.setId(7L);
        tx.setSender("ACC1");
        tx.setReceiver("ACC2");
        tx.setAmount(BigDecimal.valueOf(500));
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDeleted(false);

        when(transactionRepository.findById(7L)).thenReturn(Optional.of(tx));
        when(flaggedTransactionRepository.findByTransactionId(7L)).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(fraudDetectionService.detectFraud(any(Transaction.class))).thenReturn(List.of());

        TransactionRequest updateReq = new TransactionRequest();
        updateReq.setSender("ACC1_NEW");
        updateReq.setReceiver("ACC2_NEW");
        updateReq.setAmount(BigDecimal.valueOf(600));

        TransactionResponse res = transactionService.updateTransaction(7L, updateReq);
        assertNotNull(res);
        assertEquals("ACC1_NEW", res.getSender());
        assertEquals("ACC2_NEW", res.getReceiver());
        assertEquals(BigDecimal.valueOf(600), res.getAmount());
    }

    @Test
    void testUpdateTransaction_Deleted_ThrowsException() {
        Transaction tx = new Transaction();
        tx.setId(8L);
        tx.setDeleted(true);

        when(transactionRepository.findById(8L)).thenReturn(Optional.of(tx));

        TransactionRequest updateReq = new TransactionRequest();
        updateReq.setSender("ACC1");
        updateReq.setReceiver("ACC2");
        updateReq.setAmount(BigDecimal.valueOf(500));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> transactionService.updateTransaction(8L, updateReq));
        assertEquals("Transaction with id 8 was previously deleted and cannot be updated", ex.getMessage());
    }

    @Test
    void testUpdateTransaction_AlreadyFlagged_NoDuplicateCreated() {
        Transaction tx = new Transaction();
        tx.setId(9L);
        tx.setSender("ACC1");
        tx.setReceiver("ACC2");
        tx.setAmount(BigDecimal.valueOf(15000));
        tx.setStatus(TransactionStatus.FLAGGED);
        tx.setDeleted(false);

        FlaggedTransaction existingFt = new FlaggedTransaction();
        existingFt.setId(101L);
        existingFt.setTransaction(tx);
        existingFt.setTriggeredRules(new HashSet<>());
        existingFt.setReviewStatus(ReviewStatus.PENDING);

        Rule rule = new Rule();
        rule.setId(1L);
        rule.setName("High Amount");
        rule.setType(RuleType.HIGH_AMOUNT);

        when(transactionRepository.findById(9L)).thenReturn(Optional.of(tx));
        when(flaggedTransactionRepository.findByTransactionId(9L)).thenReturn(Optional.of(existingFt));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(fraudDetectionService.detectFraud(any(Transaction.class))).thenReturn(List.of(rule));

        TransactionRequest updateReq = new TransactionRequest();
        updateReq.setSender("ACC1");
        updateReq.setReceiver("ACC2");
        updateReq.setAmount(BigDecimal.valueOf(20000));

        TransactionResponse res = transactionService.updateTransaction(9L, updateReq);
        assertNotNull(res);
        assertEquals(TransactionStatus.FLAGGED, res.getStatus());
        // Verify ft was saved/updated rather than a duplicate created
        verify(flaggedTransactionRepository).save(existingFt);
    }

    @Test
    void testDeleteTransaction_SoftDelete() {
        Transaction tx = new Transaction();
        tx.setId(12L);
        tx.setSender("ACC1");
        tx.setReceiver("ACC2");
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setDeleted(false);

        when(transactionRepository.findById(12L)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        TransactionResponse res = transactionService.deleteTransaction(12L);
        assertNotNull(res);
        assertTrue(tx.isDeleted());
        assertEquals("Transaction with id 12 was deleted successfully", res.getMessage());
        verify(transactionRepository).save(tx);
    }
}
