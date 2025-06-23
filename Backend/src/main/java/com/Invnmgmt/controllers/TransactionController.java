package com.Invnmgmt.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Invnmgmt.dtos.Response;
import com.Invnmgmt.dtos.TransactionRequest;
import com.Invnmgmt.enums.TransactionStatus;
import com.Invnmgmt.services.TransactionService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/transactions")
public class TransactionController {
@Autowired
private final TransactionService transactionService;

// Explicit constructor for dependency injection
public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
}

@PostMapping("/purchase")
public ResponseEntity<Response> purchaseInventory(@RequestBody @Valid TransactionRequest transactionRequest) {
    return ResponseEntity.ok(transactionService.purchase(transactionRequest));
}

@PostMapping("/sell")
public ResponseEntity<Response> makeSale(@RequestBody @Valid TransactionRequest transactionRequest) {
    return ResponseEntity.ok(transactionService.sell(transactionRequest));
}

@PostMapping("/return")
public ResponseEntity<Response> returnToSupplier(@RequestBody @Valid TransactionRequest transactionRequest) {
    return ResponseEntity.ok(transactionService.returnToSupplier(transactionRequest));
}

@GetMapping("/all")
public ResponseEntity<Response> getAllTransactions(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "1000") int size,
        @RequestParam(required = false) String filter) {

    System.out.println("SEARCH VALUE IS: " + filter);
    return ResponseEntity.ok(transactionService.getAllTransactions(page, size, filter));
}

@GetMapping("/{id}")
public ResponseEntity<Response> getTransactionById(@PathVariable Long id) {
    return ResponseEntity.ok(transactionService.getAllTransactionById(id));
}

@GetMapping("/by-month-year")
public ResponseEntity<Response> getTransactionByMonthAndYear(
        @RequestParam int month,
        @RequestParam int year) {

    return ResponseEntity.ok(transactionService.getAllTransactionByMonthAndYear(month, year));
}

@PutMapping("/{transactionId}")
public ResponseEntity<Response> updateTransactionStatus(
        @PathVariable Long transactionId,
        @RequestBody TransactionStatus status) {

    return ResponseEntity.ok(transactionService.updateTransactionStatus(transactionId, status));
}
}
