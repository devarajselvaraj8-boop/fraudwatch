package com.deva.fraudwatch.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.deva.fraudwatch.entity.Transaction;
import com.deva.fraudwatch.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private Long id;
    private String sender;
    private String receiver;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private TransactionStatus status;
    private Boolean deleted;
    private String message;

    public TransactionResponse() {
    }

    public TransactionResponse(String message) {
        this.message = message;
    }

    public TransactionResponse(
            Long id,
            String sender,
            String receiver,
            BigDecimal amount,
            LocalDateTime timestamp,
            TransactionStatus status) {

        this(id, sender, receiver, amount, timestamp, status, false);
    }

    public TransactionResponse(
            Long id,
            String sender,
            String receiver,
            BigDecimal amount,
            LocalDateTime timestamp,
            TransactionStatus status,
            Boolean deleted) {

        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
        this.deleted = deleted;
    }

    public TransactionResponse(Transaction transaction) {
        if (transaction != null) {
            this.id = transaction.getId();
            this.sender = transaction.getSender();
            this.receiver = transaction.getReceiver();
            this.amount = transaction.getAmount();
            this.timestamp = transaction.getTimestamp();
            this.status = transaction.getStatus();
            this.deleted = transaction.isDeleted();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
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
