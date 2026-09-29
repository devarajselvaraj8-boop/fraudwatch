package com.deva.fraudwatch.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.deva.fraudwatch.entity.Rule;
import com.deva.fraudwatch.entity.Transaction;
import com.deva.fraudwatch.enums.RuleType;
import com.deva.fraudwatch.repository.RuleRepository;
import com.deva.fraudwatch.repository.TransactionRepository;

@Service
public class FraudDetectionService {

    private final RuleRepository ruleRepository;
    private final TransactionRepository transactionRepository;

    public FraudDetectionService(
            RuleRepository ruleRepository,
            TransactionRepository transactionRepository) {

        this.ruleRepository = ruleRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Rule> detectFraud(Transaction transaction) {

        List<Rule> triggeredRules = new ArrayList<>();
        List<Rule> activeRules = ruleRepository.findByActiveTrueAndDeletedFalse();

        System.out.println("=================================");
        System.out.println("FRAUD DETECTION");
        System.out.println("Transaction ID: " + transaction.getId() + " | Amount: " + transaction.getAmount());
        System.out.println("Sender: " + transaction.getSender() + " | Receiver: " + transaction.getReceiver());
        System.out.println("Active rules: " + activeRules.size());

        for (Rule rule : activeRules) {

            System.out.println(
                    "Checking rule: "
                    + rule.getName()
                    + " | Type: "
                    + rule.getType()
            );

            if (rule.getType() == RuleType.HIGH_AMOUNT) {
                if (isHighAmount(transaction, rule)) {
                    triggeredRules.add(rule);
                    System.out.println(">>> HIGH_AMOUNT TRIGGERED");
                }
            } else if (rule.getType() == RuleType.SENDER_VELOCITY) {
                if (isHighSenderVelocity(transaction, rule)) {
                    triggeredRules.add(rule);
                    System.out.println(">>> SENDER_VELOCITY TRIGGERED");
                }
            } else if (rule.getType() == RuleType.RECEIVER_VELOCITY) {
                if (isHighReceiverVelocity(transaction, rule)) {
                    triggeredRules.add(rule);
                    System.out.println(">>> RECEIVER_VELOCITY TRIGGERED");
                }
            }
        }

        System.out.println("Triggered rules count: " + triggeredRules.size());
        System.out.println("=================================");

        return triggeredRules;
    }

    public boolean isHighAmount(
            Transaction transaction,
            Rule rule) {

        BigDecimal threshold = rule.getAmountThreshold();

        if (threshold == null || transaction.getAmount() == null) {
            return false;
        }

        return transaction.getAmount().compareTo(threshold) > 0;
    }

    public boolean isHighSenderVelocity(
            Transaction transaction,
            Rule rule) {

        if (rule.getTransactionCount() == null
                || rule.getTimeWindowMinutes() == null
                || transaction.getSender() == null
                || transaction.getTimestamp() == null) {

            return false;
        }

        LocalDateTime endTime = transaction.getTimestamp();
        LocalDateTime startTime = endTime.minusMinutes(rule.getTimeWindowMinutes());

        long count = transactionRepository.countRecentTransactionsBySender(
                transaction.getSender(),
                startTime,
                endTime
        );

        return count >= rule.getTransactionCount();
    }

    public boolean isHighReceiverVelocity(
            Transaction transaction,
            Rule rule) {

        if (rule.getTransactionCount() == null
                || rule.getTimeWindowMinutes() == null
                || transaction.getReceiver() == null
                || transaction.getTimestamp() == null) {

            return false;
        }

        LocalDateTime endTime = transaction.getTimestamp();
        LocalDateTime startTime = endTime.minusMinutes(rule.getTimeWindowMinutes());

        long count = transactionRepository.countRecentTransactionsByReceiver(
                transaction.getReceiver(),
                startTime,
                endTime
        );

        return count >= rule.getTransactionCount();
    }

    public boolean isHighVelocity(
            Transaction transaction,
            Rule rule) {
        return isHighSenderVelocity(transaction, rule);
    }
}
