package com.deva.fraudwatch.service;

import java.math.BigDecimal;
import java.util.Optional;

import java.util.List;

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

import com.deva.fraudwatch.dto.RuleRequest;
import com.deva.fraudwatch.dto.RuleResponse;
import com.deva.fraudwatch.entity.Rule;
import com.deva.fraudwatch.enums.RuleType;
import com.deva.fraudwatch.exception.BusinessRuleException;
import com.deva.fraudwatch.exception.ResourceNotFoundException;
import com.deva.fraudwatch.repository.RuleRepository;

@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    private RuleService ruleService;

    @BeforeEach
    void setUp() {
        ruleService = new RuleService(ruleRepository);
    }

    @Test
    void testCreateRule_HighAmount_Success() {
        RuleRequest request = new RuleRequest();
        request.setName("High Amount");
        request.setType(RuleType.HIGH_AMOUNT);
        request.setAmountThreshold(BigDecimal.valueOf(10000));
        request.setActive(true);

        when(ruleRepository.save(any(Rule.class))).thenAnswer(i -> {
            Rule r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        RuleResponse response = ruleService.createRule(request);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(RuleType.HIGH_AMOUNT, response.getType());
        assertEquals(BigDecimal.valueOf(10000), response.getAmountThreshold());
        assertFalse(response.isDeleted());
    }

    @Test
    void testCreateRule_InvalidHighAmount_ThrowsException() {
        RuleRequest request = new RuleRequest();
        request.setName("High Amount");
        request.setType(RuleType.HIGH_AMOUNT);
        request.setAmountThreshold(null); // missing threshold

        assertThrows(BusinessRuleException.class, () -> ruleService.createRule(request));
    }

    @Test
    void testCreateRule_InvalidSenderVelocity_ThrowsException() {
        RuleRequest request = new RuleRequest();
        request.setName("Sender Velocity");
        request.setType(RuleType.SENDER_VELOCITY);
        request.setTransactionCount(null); // missing count
        request.setTimeWindowMinutes(10);

        assertThrows(BusinessRuleException.class, () -> ruleService.createRule(request));
    }

    @Test
    void testCreateRule_InvalidReceiverVelocity_ThrowsException() {
        RuleRequest request = new RuleRequest();
        request.setName("Receiver Velocity");
        request.setType(RuleType.RECEIVER_VELOCITY);
        request.setTransactionCount(5);
        request.setTimeWindowMinutes(null); // missing time window

        assertThrows(BusinessRuleException.class, () -> ruleService.createRule(request));
    }

    @Test
    void testGetAllRules_ReturnsOnlyNonDeletedRules() {
        Rule r1 = new Rule();
        r1.setId(1L);
        r1.setName("Active Rule");
        r1.setActive(true);
        r1.setDeleted(false);

        Rule r2 = new Rule();
        r2.setId(2L);
        r2.setName("Disabled Rule");
        r2.setActive(false);
        r2.setDeleted(false);

        when(ruleRepository.findByDeletedFalse()).thenReturn(List.of(r1, r2));

        List<RuleResponse> rules = ruleService.getAllRules();
        assertEquals(2, rules.size());
        assertEquals("Active Rule", rules.get(0).getName());
        assertEquals("Disabled Rule", rules.get(1).getName());
    }

    @Test
    void testDeleteRule_SoftDelete_SetsActiveFalseAndDeletedTrue() {
        Rule rule = new Rule();
        rule.setId(1L);
        rule.setName("Old Rule");
        rule.setActive(true);
        rule.setDeleted(false);

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(any(Rule.class))).thenAnswer(i -> i.getArgument(0));

        RuleResponse response = ruleService.deleteRule(1L);
        assertNotNull(response);
        assertFalse(response.isActive());
        assertTrue(rule.isDeleted());
        assertEquals("Rule with id 1 was deleted successfully", response.getMessage());
        verify(ruleRepository).save(rule);
    }

    @Test
    void testGetRuleById_DeletedRule_ReturnsPreviouslyDeletedMessage() {
        Rule rule = new Rule();
        rule.setId(3L);
        rule.setName("Deleted Rule");
        rule.setActive(false);
        rule.setDeleted(true);

        when(ruleRepository.findById(3L)).thenReturn(Optional.of(rule));

        RuleResponse response = ruleService.getRuleById(3L);
        assertNotNull(response);
        assertEquals("Rule with id 3 was previously deleted", response.getMessage());
    }

    @Test
    void testGetRuleById_NotFound_ThrowsResourceNotFoundException() {
        when(ruleRepository.findById(404L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> ruleService.getRuleById(404L));
        assertEquals("Rule with id 404 not found", ex.getMessage());
    }

    @Test
    void testUpdateRule_DeletedRule_ThrowsBusinessRuleException() {
        Rule rule = new Rule();
        rule.setId(3L);
        rule.setName("Deleted Rule");
        rule.setActive(false);
        rule.setDeleted(true);

        when(ruleRepository.findById(3L)).thenReturn(Optional.of(rule));

        RuleRequest request = new RuleRequest();
        request.setName("Updated");
        request.setType(RuleType.HIGH_AMOUNT);
        request.setAmountThreshold(BigDecimal.valueOf(5000));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> ruleService.updateRule(3L, request));
        assertEquals("Rule with id 3 was previously deleted and cannot be updated", ex.getMessage());
    }

    @Test
    void testToggleRuleActive_DeletedRule_ThrowsBusinessRuleException() {
        Rule rule = new Rule();
        rule.setId(3L);
        rule.setName("Deleted Rule");
        rule.setActive(false);
        rule.setDeleted(true);

        when(ruleRepository.findById(3L)).thenReturn(Optional.of(rule));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> ruleService.toggleRuleActive(3L, true));
        assertEquals("Rule with id 3 was previously deleted and cannot be modified", ex.getMessage());
    }

    @Test
    void testToggleRuleActive_Success() {
        Rule rule = new Rule();
        rule.setId(1L);
        rule.setName("Rule 1");
        rule.setActive(true);
        rule.setDeleted(false);

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(any(Rule.class))).thenAnswer(i -> i.getArgument(0));

        RuleResponse response = ruleService.toggleRuleActive(1L, false);
        assertNotNull(response);
        assertFalse(response.isActive());
        assertFalse(rule.isActive());
    }
}
