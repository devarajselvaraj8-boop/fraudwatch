package com.deva.fraudwatch.dto;

import java.math.BigDecimal;

import com.deva.fraudwatch.entity.Rule;
import com.deva.fraudwatch.enums.RuleType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleResponse {

    private Long id;
    private String name;
    private RuleType type;
    private BigDecimal amountThreshold;
    private Integer transactionCount;
    private Integer timeWindowMinutes;
    private Boolean active;
    private Boolean deleted;
    private String message;

    public RuleResponse() {
    }

    public RuleResponse(String message) {
        this.message = message;
    }

    public RuleResponse(Long id, String name, RuleType type, BigDecimal amountThreshold, Integer transactionCount, Integer timeWindowMinutes, boolean active) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.amountThreshold = amountThreshold;
        this.transactionCount = transactionCount;
        this.timeWindowMinutes = timeWindowMinutes;
        this.active = active;
        this.deleted = false;
    }

    public RuleResponse(Long id, String name, RuleType type, BigDecimal amountThreshold, Integer transactionCount, Integer timeWindowMinutes, boolean active, boolean deleted) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.amountThreshold = amountThreshold;
        this.transactionCount = transactionCount;
        this.timeWindowMinutes = timeWindowMinutes;
        this.active = active;
        this.deleted = deleted;
    }

    public RuleResponse(Rule rule) {
        if (rule != null) {
            this.id = rule.getId();
            this.name = rule.getName();
            this.type = rule.getType();
            this.amountThreshold = rule.getAmountThreshold();
            this.transactionCount = rule.getTransactionCount();
            this.timeWindowMinutes = rule.getTimeWindowMinutes();
            this.active = rule.isActive();
            this.deleted = rule.isDeleted();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = type;
    }

    public BigDecimal getAmountThreshold() {
        return amountThreshold;
    }

    public void setAmountThreshold(BigDecimal amountThreshold) {
        this.amountThreshold = amountThreshold;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Integer getTimeWindowMinutes() {
        return timeWindowMinutes;
    }

    public void setTimeWindowMinutes(Integer timeWindowMinutes) {
        this.timeWindowMinutes = timeWindowMinutes;
    }

    public Boolean isActive() {
        return active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
