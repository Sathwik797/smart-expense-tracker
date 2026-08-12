package com.sathwik.expensetracker.controller;

import com.sathwik.expensetracker.dto.DashboardResponse;
import com.sathwik.expensetracker.dto.ExpenseRequest;
import com.sathwik.expensetracker.dto.ExpenseResponse;
import com.sathwik.expensetracker.dto.ExpenseSearchRequest;
import com.sathwik.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.addExpense(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExpenseResponse>> getUserExpenses(@PathVariable Long userId) {
        List<ExpenseResponse> expenses = expenseService.getUserExpenses(userId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<ExpenseResponse>> searchExpenses(
            @PathVariable Long userId,
            @ModelAttribute ExpenseSearchRequest searchRequest) {
        List<ExpenseResponse> results = expenseService.searchExpenses(userId, searchRequest);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        ExpenseResponse expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse updatedExpense = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable Long userId) {
        DashboardResponse dashboard = expenseService.getDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }
}
