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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.deva.fraudwatch.dto.TransactionRequest;
import com.deva.fraudwatch.dto.TransactionResponse;
import com.deva.fraudwatch.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(
            @Valid @RequestBody TransactionRequest request) {

        return transactionService.createTransaction(request);
    }

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        if (response.getMessage() != null) {
            return ResponseEntity.ok(Collections.singletonMap("message", response.getMessage()));
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public TransactionResponse updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {

        return transactionService.updateTransaction(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(@PathVariable Long id) {
        TransactionResponse response = transactionService.deleteTransaction(id);
        return ResponseEntity.ok(Collections.singletonMap("message", response.getMessage()));
    }
}
