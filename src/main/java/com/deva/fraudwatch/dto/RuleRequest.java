package com.deva.fraudwatch.dto;

import com.deva.fraudwatch.enums.RuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RuleRequest {

    @NotBlank
    private String name;

    @NotNull
    private RuleType type;

    private BigDecimal amountThreshold;

    private Integer transactionCount;

    private Integer timeWindowMinutes;

    private boolean active;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
