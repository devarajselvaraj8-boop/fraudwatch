package com.deva.fraudwatch.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deva.fraudwatch.dto.RuleRequest;
import com.deva.fraudwatch.dto.RuleResponse;
import com.deva.fraudwatch.entity.Rule;
import com.deva.fraudwatch.enums.RuleType;
import com.deva.fraudwatch.exception.BusinessRuleException;
import com.deva.fraudwatch.exception.ResourceNotFoundException;
import com.deva.fraudwatch.repository.RuleRepository;

@Service
public class RuleService {

    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public RuleResponse createRule(RuleRequest request) {
        validateRuleRequest(request);

        Rule rule = new Rule();
        rule.setName(request.getName());
        rule.setType(request.getType());
        rule.setAmountThreshold(request.getAmountThreshold());
        rule.setTransactionCount(request.getTransactionCount());
        rule.setTimeWindowMinutes(request.getTimeWindowMinutes());
        rule.setActive(request.isActive());
        rule.setDeleted(false);

        Rule saved = ruleRepository.save(rule);
        return new RuleResponse(saved);
    }

    public List<RuleResponse> getAllRules() {
        return ruleRepository.findByDeletedFalse()
                .stream()
                .map(RuleResponse::new)
                .toList();
    }

    public RuleResponse getRuleById(Long id) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule with id " + id + " not found"));

        if (rule.isDeleted()) {
            RuleResponse response = new RuleResponse();
            response.setMessage("Rule with id " + id + " was previously deleted");
            return response;
        }

        return new RuleResponse(rule);
    }

    @Transactional
    public RuleResponse updateRule(Long id, RuleRequest request) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule with id " + id + " not found"));

        if (rule.isDeleted()) {
            throw new BusinessRuleException("Rule with id " + id + " was previously deleted and cannot be updated");
        }

        validateRuleRequest(request);

        rule.setName(request.getName());
        rule.setType(request.getType());
        rule.setAmountThreshold(request.getAmountThreshold());
        rule.setTransactionCount(request.getTransactionCount());
        rule.setTimeWindowMinutes(request.getTimeWindowMinutes());
        rule.setActive(request.isActive());

        Rule updated = ruleRepository.save(rule);
        return new RuleResponse(updated);
    }

    @Transactional
    public RuleResponse toggleRuleActive(Long id, boolean active) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule with id " + id + " not found"));

        if (rule.isDeleted()) {
            throw new BusinessRuleException("Rule with id " + id + " was previously deleted and cannot be modified");
        }

        rule.setActive(active);
        Rule saved = ruleRepository.save(rule);
        return new RuleResponse(saved);
    }

    @Transactional
    public RuleResponse deleteRule(Long id) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule with id " + id + " not found"));

        rule.setActive(false);
        rule.setDeleted(true);
        Rule saved = ruleRepository.save(rule);

        RuleResponse response = new RuleResponse(saved);
        response.setMessage("Rule with id " + id + " was deleted successfully");
        return response;
    }

    private void validateRuleRequest(RuleRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessRuleException("Rule name cannot be blank");
        }

        if (request.getType() == null) {
            throw new BusinessRuleException("Rule type cannot be null");
        }

        if (request.getType() == RuleType.HIGH_AMOUNT) {
            if (request.getAmountThreshold() == null || request.getAmountThreshold().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("HIGH_AMOUNT rule requires a positive amountThreshold");
            }
        } else if (request.getType() == RuleType.VELOCITY) {
            if (request.getTransactionCount() == null || request.getTransactionCount() <= 0) {
                throw new BusinessRuleException("VELOCITY rule requires a positive transactionCount");
            }
            if (request.getTimeWindowMinutes() == null || request.getTimeWindowMinutes() <= 0) {
                throw new BusinessRuleException("VELOCITY rule requires a positive timeWindowMinutes");
            }
        }
    }
}
