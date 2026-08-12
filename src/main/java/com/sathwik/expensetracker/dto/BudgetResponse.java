package com.sathwik.expensetracker.dto;

import com.sathwik.expensetracker.entity.Budget;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BudgetResponse {

    private Long id;
    private Long userId;
    private String category;
    private BigDecimal monthlyLimit;
    private String period;
    private LocalDateTime createdAt;

    public BudgetResponse() {
    }

    public static BudgetResponse fromEntity(Budget budget) {
        BudgetResponse response = new BudgetResponse();
        response.setId(budget.getId());
        response.setUserId(budget.getUser() != null ? budget.getUser().getId() : null);
        response.setCategory(budget.getCategory());
        response.setMonthlyLimit(budget.getMonthlyLimit());
        response.setPeriod(budget.getPeriod());
        response.setCreatedAt(budget.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
