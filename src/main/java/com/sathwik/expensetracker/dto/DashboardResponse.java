package com.sathwik.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class DashboardResponse {

    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private Map<String, BigDecimal> categoryWise;
    private String topCategory;
    private Map<LocalDate, BigDecimal> dailySpending;
    private String insight;

    public DashboardResponse() {
    }

    public DashboardResponse(BigDecimal totalExpense, BigDecimal totalIncome, Map<String, BigDecimal> categoryWise, String topCategory, Map<LocalDate, BigDecimal> dailySpending, String insight) {
        this.totalExpense = totalExpense;
        this.totalIncome = totalIncome;
        this.categoryWise = categoryWise;
        this.topCategory = topCategory;
        this.dailySpending = dailySpending;
        this.insight = insight;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public Map<String, BigDecimal> getCategoryWise() {
        return categoryWise;
    }

    public void setCategoryWise(Map<String, BigDecimal> categoryWise) {
        this.categoryWise = categoryWise;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(String topCategory) {
        this.topCategory = topCategory;
    }

    public Map<LocalDate, BigDecimal> getDailySpending() {
        return dailySpending;
    }

    public void setDailySpending(Map<LocalDate, BigDecimal> dailySpending) {
        this.dailySpending = dailySpending;
    }

    public String getInsight() {
        return insight;
    }

    public void setInsight(String insight) {
        this.insight = insight;
    }
}
