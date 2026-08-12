package com.sathwik.expensetracker.dto;

import com.sathwik.expensetracker.enums.BudgetStatus;
import java.math.BigDecimal;

public class BudgetStatusResponse {

    private Long id;
    private Long userId;
    private String category;
    private String period;
    private BigDecimal monthlyLimit;
    private BigDecimal amountSpent;
    private BigDecimal remainingAmount;
    private BigDecimal percentageUsed;
    private BudgetStatus status;

    public BudgetStatusResponse() {
    }

    public BudgetStatusResponse(Long id, Long userId, String category, String period, BigDecimal monthlyLimit, BigDecimal amountSpent, BigDecimal remainingAmount, BigDecimal percentageUsed, BudgetStatus status) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.period = period;
        this.monthlyLimit = monthlyLimit;
        this.amountSpent = amountSpent;
        this.remainingAmount = remainingAmount;
        this.percentageUsed = percentageUsed;
        this.status = status;
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

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public BigDecimal getAmountSpent() {
        return amountSpent;
    }

    public void setAmountSpent(BigDecimal amountSpent) {
        this.amountSpent = amountSpent;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public BigDecimal getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(BigDecimal percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public BudgetStatus getStatus() {
        return status;
    }

    public void setStatus(BudgetStatus status) {
        this.status = status;
    }
}
