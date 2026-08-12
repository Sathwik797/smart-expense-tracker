package com.sathwik.expensetracker.controller;

import com.sathwik.expensetracker.dto.BudgetRequest;
import com.sathwik.expensetracker.dto.BudgetResponse;
import com.sathwik.expensetracker.dto.BudgetStatusResponse;
import com.sathwik.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.createBudget(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BudgetResponse>> getUserBudgets(@PathVariable Long userId) {
        List<BudgetResponse> budgets = budgetService.getUserBudgets(userId);
        return ResponseEntity.ok(budgets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(@PathVariable Long id, @Valid @RequestBody BudgetRequest request) {
        BudgetResponse updated = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
    }

    @GetMapping("/user/{userId}/status")
    public ResponseEntity<List<BudgetStatusResponse>> getBudgetStatus(
            @PathVariable Long userId,
            @RequestParam(required = false) String period) {
        List<BudgetStatusResponse> statusList = budgetService.getBudgetStatus(userId, period);
        return ResponseEntity.ok(statusList);
    }
}
