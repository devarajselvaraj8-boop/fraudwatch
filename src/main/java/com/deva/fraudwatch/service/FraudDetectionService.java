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
        System.out.println("Transaction amount: " + transaction.getAmount());
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

            } else if (rule.getType() == RuleType.VELOCITY) {

                if (isHighVelocity(transaction, rule)) {
                    triggeredRules.add(rule);

                    System.out.println(">>> VELOCITY TRIGGERED");
                }
            }
        }

        System.out.println("Triggered rules: " + triggeredRules.size());
        System.out.println("=================================");

        return triggeredRules;
    }

    private boolean isHighAmount(
            Transaction transaction,
            Rule rule) {

        BigDecimal threshold = rule.getAmountThreshold();

        if (threshold == null) {
            return false;
        }

        return transaction.getAmount().compareTo(threshold) > 0;
    }

    private boolean isHighVelocity(
            Transaction transaction,
            Rule rule) {

        if (rule.getTransactionCount() == null
                || rule.getTimeWindowMinutes() == null) {

            return false;
        }

        LocalDateTime endTime = transaction.getTimestamp();

        LocalDateTime startTime
                = endTime.minusMinutes(
                        rule.getTimeWindowMinutes()
                );

        long count
                = transactionRepository.countRecentTransactions(
                        transaction.getSender(),
                        startTime,
                        endTime
                );

        return count >= rule.getTransactionCount();
    }
}
