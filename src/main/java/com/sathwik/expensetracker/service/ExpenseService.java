package com.sathwik.expensetracker.service;

import com.sathwik.expensetracker.dto.DashboardResponse;
import com.sathwik.expensetracker.dto.ExpenseRequest;
import com.sathwik.expensetracker.dto.ExpenseResponse;
import com.sathwik.expensetracker.entity.Expense;
import com.sathwik.expensetracker.entity.User;
import com.sathwik.expensetracker.enums.ExpenseType;
import com.sathwik.expensetracker.exception.ResourceNotFoundException;
import com.sathwik.expensetracker.repository.ExpenseRepository;
import com.sathwik.expensetracker.repository.UserRepository;
import com.sathwik.expensetracker.repository.specification.ExpenseSpecification;
import com.sathwik.expensetracker.dto.ExpenseSearchRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExpenseResponse addExpense(ExpenseRequest request) {
        User user = getOrCreateUser(request.getUserId());

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory().trim());
        expense.setType(request.getType());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        expense.setDate(request.getDate());

        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(savedExpense);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getUserExpenses(Long userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId)
                .stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> searchExpenses(Long userId, ExpenseSearchRequest request) {
        if (request != null) {
            request.validate();
        }
        Specification<Expense> spec = ExpenseSpecification.buildSpecification(userId, request);
        return expenseRepository.findAll(spec)
                .stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));

        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory().trim());
        expense.setType(request.getType());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        expense.setDate(request.getDate());

        Expense updatedExpense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(updatedExpense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpense(Long userId) {
        BigDecimal total = expenseRepository.sumAmountByUserIdAndType(userId, ExpenseType.EXPENSE);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalIncome(Long userId) {
        BigDecimal total = expenseRepository.sumAmountByUserIdAndType(userId, ExpenseType.INCOME);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCategoryWise(Long userId) {
        List<Object[]> rows = expenseRepository.findCategoryTotalsByUserIdAndType(userId, ExpenseType.EXPENSE);
        Map<String, BigDecimal> categoryMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            categoryMap.put(category, amount != null ? amount : BigDecimal.ZERO);
        }
        return categoryMap;
    }

    @Transactional(readOnly = true)
    public String getTopCategory(Long userId) {
        Map<String, BigDecimal> map = getCategoryWise(userId);
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, BigDecimal> getDailySpending(Long userId) {
        List<Expense> expenses = expenseRepository.findByUserIdAndType(userId, ExpenseType.EXPENSE);
        Map<LocalDate, BigDecimal> dailyMap = new TreeMap<>();

        for (Expense expense : expenses) {
            LocalDate date = expense.getDate();
            BigDecimal currentTotal = dailyMap.getOrDefault(date, BigDecimal.ZERO);
            dailyMap.put(date, currentTotal.add(expense.getAmount()));
        }
        return dailyMap;
    }

    @Transactional(readOnly = true)
    public String getSmartInsight(Long userId) {
        BigDecimal totalExpense = getTotalExpense(userId);
        BigDecimal totalIncome = getTotalIncome(userId);
        Map<String, BigDecimal> categoryWise = getCategoryWise(userId);

        if (totalExpense.compareTo(BigDecimal.ZERO) == 0 && totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            return "No financial data recorded yet. Start tracking your income and expenses!";
        }

        if (totalExpense.compareTo(BigDecimal.ZERO) == 0) {
            return "Great start! You have income recorded and zero expenses so far.";
        }

        String topCategory = getTopCategory(userId);
        BigDecimal topCategoryAmount = categoryWise.getOrDefault(topCategory, BigDecimal.ZERO);

        BigDecimal topCategoryPercentage = topCategoryAmount.multiply(new BigDecimal("100"))
                .divide(totalExpense, 1, RoundingMode.HALF_UP);

        if (totalIncome.compareTo(BigDecimal.ZERO) > 0 && totalExpense.compareTo(totalIncome) > 0) {
            return "Warning: Your total expenses (₹" + totalExpense + ") exceed your income (₹" + totalIncome + ")! Top spend category is " + topCategory + " (" + topCategoryPercentage + "% of expenses).";
        }

        if (topCategoryPercentage.compareTo(new BigDecimal("40.0")) > 0) {
            return "Budget Alert: Major spending concentrated in '" + topCategory + "' (" + topCategoryPercentage + "% of total expenses). Consider reducing non-essential costs here.";
        }

        return "Healthy spending distribution! Your top category is '" + topCategory + "' accounting for " + topCategoryPercentage + "% of total expenses.";
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        BigDecimal totalExpense = getTotalExpense(userId);
        BigDecimal totalIncome = getTotalIncome(userId);
        Map<String, BigDecimal> categoryWise = getCategoryWise(userId);
        String topCategory = getTopCategory(userId);
        Map<LocalDate, BigDecimal> dailySpending = getDailySpending(userId);
        String insight = getSmartInsight(userId);

        return new DashboardResponse(totalExpense, totalIncome, categoryWise, topCategory, dailySpending, insight);
    }

    private User getOrCreateUser(Long userId) {
        return userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User();
            newUser.setFullName("Demo User");
            newUser.setEmail("user" + userId + "@example.com");
            return userRepository.save(newUser);
        });
    }
}
