package com.sathwik.expensetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class BudgetRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Monthly limit is required")
    @Positive(message = "Monthly limit must be greater than zero")
    private BigDecimal monthlyLimit;

    @NotBlank(message = "Period is required")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Period must strictly follow YYYY-MM format")
    private String period;

    public BudgetRequest() {
    }

    public BudgetRequest(Long userId, String category, BigDecimal monthlyLimit, String period) {
        this.userId = userId;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.period = period;
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
}
