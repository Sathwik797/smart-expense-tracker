package com.sathwik.expensetracker.service;

import com.sathwik.expensetracker.dto.BudgetRequest;
import com.sathwik.expensetracker.dto.BudgetResponse;
import com.sathwik.expensetracker.dto.BudgetStatusResponse;
import com.sathwik.expensetracker.entity.Budget;
import com.sathwik.expensetracker.entity.Expense;
import com.sathwik.expensetracker.entity.User;
import com.sathwik.expensetracker.enums.BudgetStatus;
import com.sathwik.expensetracker.enums.ExpenseType;
import com.sathwik.expensetracker.exception.ResourceNotFoundException;
import com.sathwik.expensetracker.repository.BudgetRepository;
import com.sathwik.expensetracker.repository.ExpenseRepository;
import com.sathwik.expensetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository, ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BudgetResponse createBudget(BudgetRequest request) {
        String category = request.getCategory().trim();
        String period = request.getPeriod().trim();

        if (budgetRepository.existsByUserIdAndCategoryIgnoreCaseAndPeriod(request.getUserId(), category, period)) {
            throw new IllegalArgumentException("Budget already exists for user " + request.getUserId() + " in category '" + category + "' for period " + period);
        }

        User user = userRepository.findById(request.getUserId()).orElseGet(() -> {
            User newUser = new User();
            newUser.setFullName("Demo User");
            newUser.setEmail("user" + request.getUserId() + "@example.com");
            return userRepository.save(newUser);
        });

        Budget budget = new Budget(user, category, request.getMonthlyLimit(), period);
        Budget savedBudget = budgetRepository.save(budget);
        return BudgetResponse.fromEntity(savedBudget);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getUserBudgets(Long userId) {
        return budgetRepository.findByUserId(userId)
                .stream()
                .map(BudgetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID: " + id));

        String category = request.getCategory().trim();
        String period = request.getPeriod().trim();

        if (budgetRepository.existsByUserIdAndCategoryIgnoreCaseAndPeriodAndIdNot(request.getUserId(), category, period, id)) {
            throw new IllegalArgumentException("Budget already exists for user " + request.getUserId() + " in category '" + category + "' for period " + period);
        }

        budget.setCategory(category);
        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setPeriod(period);

        Budget updated = budgetRepository.save(budget);
        return BudgetResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID: " + id));
        budgetRepository.delete(budget);
    }

    @Transactional(readOnly = true)
    public List<BudgetStatusResponse> getBudgetStatus(Long userId, String period) {
        if (period == null || period.trim().isEmpty()) {
            period = YearMonth.now().toString();
        } else {
            period = period.trim();
        }

        YearMonth ym;
        try {
            ym = YearMonth.parse(period);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Period must strictly follow YYYY-MM format");
        }

        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<Budget> userBudgets = budgetRepository.findByUserIdAndPeriod(userId, period);
        List<Expense> monthExpenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        List<BudgetStatusResponse> statusList = new ArrayList<>();

        for (Budget budget : userBudgets) {
            BigDecimal spent = monthExpenses.stream()
                    .filter(e -> e.getType() == ExpenseType.EXPENSE && e.getCategory().equalsIgnoreCase(budget.getCategory()))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal limit = budget.getMonthlyLimit();
            BigDecimal remaining = limit.subtract(spent);

            BigDecimal percentage;
            if (limit.compareTo(BigDecimal.ZERO) == 0) {
                percentage = BigDecimal.ZERO;
            } else {
                percentage = spent.multiply(new BigDecimal("100")).divide(limit, 2, RoundingMode.HALF_UP);
            }

            BudgetStatus status;
            if (percentage.compareTo(new BigDecimal("80.00")) < 0) {
                status = BudgetStatus.UNDER_BUDGET;
            } else if (percentage.compareTo(new BigDecimal("100.00")) < 0) {
                status = BudgetStatus.NEAR_LIMIT;
            } else {
                status = BudgetStatus.EXCEEDED;
            }

            statusList.add(new BudgetStatusResponse(
                    budget.getId(),
                    userId,
                    budget.getCategory(),
                    period,
                    limit,
                    spent,
                    remaining,
                    percentage,
                    status
            ));
        }

        return statusList;
    }
}
