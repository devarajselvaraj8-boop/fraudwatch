package com.deva.fraudwatch.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.deva.fraudwatch.entity.Rule;
import com.deva.fraudwatch.enums.RuleType;
import com.deva.fraudwatch.repository.RuleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RuleRepository ruleRepository;

    public DataInitializer(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public void run(String... args) {
        initDefaultRules();
    }

    private void initDefaultRules() {
        List<Rule> allRules = ruleRepository.findAll();

        // 1. Ensure HIGH_AMOUNT default rule
        Rule highAmountRule = allRules.stream()
                .filter(r -> r.getType() == RuleType.HIGH_AMOUNT && !r.isDeleted())
                .findFirst()
                .orElse(null);

        if (highAmountRule == null) {
            Rule deletedHighAmount = allRules.stream()
                    .filter(r -> r.getType() == RuleType.HIGH_AMOUNT)
                    .findFirst()
                    .orElse(null);

            if (deletedHighAmount != null) {
                deletedHighAmount.setName("High Amount Transaction");
                deletedHighAmount.setAmountThreshold(BigDecimal.valueOf(10000));
                deletedHighAmount.setActive(true);
                deletedHighAmount.setDeleted(false);
                ruleRepository.save(deletedHighAmount);
            } else {
                Rule rule1 = new Rule();
                rule1.setName("High Amount Transaction");
                rule1.setType(RuleType.HIGH_AMOUNT);
                rule1.setAmountThreshold(BigDecimal.valueOf(10000));
                rule1.setActive(true);
                rule1.setDeleted(false);
                ruleRepository.save(rule1);
            }
        }

        // 2. Ensure SENDER_VELOCITY default rule
        Rule senderVelocityRule = allRules.stream()
                .filter(r -> r.getType() == RuleType.SENDER_VELOCITY && !r.isDeleted())
                .findFirst()
                .orElse(null);

        if (senderVelocityRule == null) {
            Rule deletedSenderVelocity = allRules.stream()
                    .filter(r -> r.getType() == RuleType.SENDER_VELOCITY)
                    .findFirst()
                    .orElse(null);

            if (deletedSenderVelocity != null) {
                deletedSenderVelocity.setName("Sender Transaction Velocity");
                deletedSenderVelocity.setTransactionCount(5);
                deletedSenderVelocity.setTimeWindowMinutes(10);
                deletedSenderVelocity.setActive(true);
                deletedSenderVelocity.setDeleted(false);
                ruleRepository.save(deletedSenderVelocity);
            } else {
                Rule rule2 = new Rule();
                rule2.setName("Sender Transaction Velocity");
                rule2.setType(RuleType.SENDER_VELOCITY);
                rule2.setTransactionCount(5);
                rule2.setTimeWindowMinutes(10);
                rule2.setActive(true);
                rule2.setDeleted(false);
                ruleRepository.save(rule2);
            }
        }

        // 3. Ensure RECEIVER_VELOCITY default rule
        Rule receiverVelocityRule = allRules.stream()
                .filter(r -> r.getType() == RuleType.RECEIVER_VELOCITY && !r.isDeleted())
                .findFirst()
                .orElse(null);

        if (receiverVelocityRule == null) {
            Rule deletedReceiverVelocity = allRules.stream()
                    .filter(r -> r.getType() == RuleType.RECEIVER_VELOCITY)
                    .findFirst()
                    .orElse(null);

            if (deletedReceiverVelocity != null) {
                deletedReceiverVelocity.setName("Receiver Transaction Velocity");
                deletedReceiverVelocity.setTransactionCount(5);
                deletedReceiverVelocity.setTimeWindowMinutes(10);
                deletedReceiverVelocity.setActive(true);
                deletedReceiverVelocity.setDeleted(false);
                ruleRepository.save(deletedReceiverVelocity);
            } else {
                Rule rule3 = new Rule();
                rule3.setName("Receiver Transaction Velocity");
                rule3.setType(RuleType.RECEIVER_VELOCITY);
                rule3.setTransactionCount(5);
                rule3.setTimeWindowMinutes(10);
                rule3.setActive(true);
                rule3.setDeleted(false);
                ruleRepository.save(rule3);
            }
        }
    }
}
