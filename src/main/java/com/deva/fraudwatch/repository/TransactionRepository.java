package com.deva.fraudwatch.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.deva.fraudwatch.entity.Transaction;
import com.deva.fraudwatch.enums.TransactionStatus;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.deleted = false OR t.deleted IS NULL ORDER BY t.timestamp DESC, t.id DESC")
    List<Transaction> findByDeletedFalse();

    long countByStatus(TransactionStatus status);

    @Query("""
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.sender = :sender
        AND (t.deleted = false OR t.deleted IS NULL)
        AND t.timestamp >= :startTime
        AND t.timestamp <= :endTime
    """)
    long countRecentTransactionsBySender(
            @Param("sender") String sender,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.receiver = :receiver
        AND (t.deleted = false OR t.deleted IS NULL)
        AND t.timestamp >= :startTime
        AND t.timestamp <= :endTime
    """)
    long countRecentTransactionsByReceiver(
            @Param("receiver") String receiver,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.sender = :sender
        AND (t.deleted = false OR t.deleted IS NULL)
        AND t.timestamp >= :startTime
        AND t.timestamp <= :endTime
    """)
    long countRecentTransactions(
            @Param("sender") String sender,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
