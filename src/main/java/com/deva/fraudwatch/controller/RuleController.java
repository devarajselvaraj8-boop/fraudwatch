package com.deva.fraudwatch.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.deva.fraudwatch.dto.RuleRequest;
import com.deva.fraudwatch.dto.RuleResponse;
import com.deva.fraudwatch.service.RuleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RuleResponse createRule(@Valid @RequestBody RuleRequest request) {
        return ruleService.createRule(request);
    }

    @GetMapping
    public List<RuleResponse> getAllRules() {
        return ruleService.getAllRules();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRuleById(@PathVariable Long id) {
        RuleResponse response = ruleService.getRuleById(id);
        if (response.getMessage() != null) {
            return ResponseEntity.ok(Collections.singletonMap("message", response.getMessage()));
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public RuleResponse updateRule(
            @PathVariable Long id,
            @Valid @RequestBody RuleRequest request) {
        return ruleService.updateRule(id, request);
    }

    @org.springframework.web.bind.annotation.RequestMapping(value = "/{id}/enable", method = {org.springframework.web.bind.annotation.RequestMethod.PUT, org.springframework.web.bind.annotation.RequestMethod.PATCH})
    public RuleResponse enableRule(@PathVariable Long id) {
        return ruleService.toggleRuleActive(id, true);
    }

    @org.springframework.web.bind.annotation.RequestMapping(value = "/{id}/disable", method = {org.springframework.web.bind.annotation.RequestMethod.PUT, org.springframework.web.bind.annotation.RequestMethod.PATCH})
    public RuleResponse disableRule(@PathVariable Long id) {
        return ruleService.toggleRuleActive(id, false);
    }

    @org.springframework.web.bind.annotation.RequestMapping(value = "/{id}/status", method = {org.springframework.web.bind.annotation.RequestMethod.PUT, org.springframework.web.bind.annotation.RequestMethod.PATCH})
    public RuleResponse setRuleStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ruleService.toggleRuleActive(id, active);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRule(@PathVariable Long id) {
        RuleResponse response = ruleService.deleteRule(id);
        return ResponseEntity.ok(Collections.singletonMap("message", response.getMessage()));
    }
}
