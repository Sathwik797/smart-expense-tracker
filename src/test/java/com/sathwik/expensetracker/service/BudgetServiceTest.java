package com.sathwik.expensetracker.service;

import com.sathwik.expensetracker.dto.BudgetRequest;
import com.sathwik.expensetracker.dto.BudgetResponse;
import com.sathwik.expensetracker.dto.BudgetStatusResponse;
import com.sathwik.expensetracker.entity.Budget;
import com.sathwik.expensetracker.entity.Expense;
import com.sathwik.expensetracker.entity.User;
import com.sathwik.expensetracker.enums.BudgetStatus;
import com.sathwik.expensetracker.enums.ExpenseType;
import com.sathwik.expensetracker.enums.PaymentMethod;
import com.sathwik.expensetracker.exception.ResourceNotFoundException;
import com.sathwik.expensetracker.repository.BudgetRepository;
import com.sathwik.expensetracker.repository.ExpenseRepository;
import com.sathwik.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User sampleUser;
    private Budget sampleBudget;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Demo User", "user1@example.com");
        sampleBudget = new Budget(sampleUser, "Food", new BigDecimal("5000.00"), "2026-08");
        sampleBudget.setId(10L);
    }

    @Test
    void createBudget_Success() {
        BudgetRequest request = new BudgetRequest(1L, "Food", new BigDecimal("5000.00"), "2026-08");

        when(budgetRepository.existsByUserIdAndCategoryIgnoreCaseAndPeriod(1L, "Food", "2026-08")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(budgetRepository.save(any(Budget.class))).thenReturn(sampleBudget);

        BudgetResponse response = budgetService.createBudget(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Food", response.getCategory());
        assertEquals(new BigDecimal("5000.00"), response.getMonthlyLimit());
    }

    @Test
    void createBudget_Duplicate_ThrowsIllegalArgumentException() {
        BudgetRequest request = new BudgetRequest(1L, "Food", new BigDecimal("5000.00"), "2026-08");

        when(budgetRepository.existsByUserIdAndCategoryIgnoreCaseAndPeriod(1L, "Food", "2026-08")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> budgetService.createBudget(request));
    }

    @Test
    void updateBudget_Success() {
        BudgetRequest updateRequest = new BudgetRequest(1L, "Food", new BigDecimal("6000.00"), "2026-08");

        when(budgetRepository.findById(10L)).thenReturn(Optional.of(sampleBudget));
        when(budgetRepository.existsByUserIdAndCategoryIgnoreCaseAndPeriodAndIdNot(1L, "Food", "2026-08", 10L)).thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenReturn(sampleBudget);

        BudgetResponse response = budgetService.updateBudget(10L, updateRequest);

        assertNotNull(response);
        verify(budgetRepository, times(1)).save(sampleBudget);
    }

    @Test
    void deleteBudget_Success() {
        when(budgetRepository.findById(10L)).thenReturn(Optional.of(sampleBudget));
        doNothing().when(budgetRepository).delete(sampleBudget);

        budgetService.deleteBudget(10L);

        verify(budgetRepository, times(1)).delete(sampleBudget);
    }

    @Test
    void deleteBudget_NotFound_ThrowsException() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> budgetService.deleteBudget(99L));
    }

    @Test
    void getBudgetStatus_UnderBudget() {
        Expense expense = new Expense();
        expense.setAmount(new BigDecimal("2000.00")); // 40% of 5000
        expense.setCategory("Food");
        expense.setType(ExpenseType.EXPENSE);
        expense.setDate(LocalDate.of(2026, 8, 10));

        when(budgetRepository.findByUserIdAndPeriod(1L, "2026-08")).thenReturn(List.of(sampleBudget));
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(expense));

        List<BudgetStatusResponse> statusList = budgetService.getBudgetStatus(1L, "2026-08");

        assertEquals(1, statusList.size());
        BudgetStatusResponse status = statusList.get(0);
        assertEquals(BudgetStatus.UNDER_BUDGET, status.getStatus());
        assertEquals(0, new BigDecimal("40.00").compareTo(status.getPercentageUsed()));
    }

    @Test
    void getBudgetStatus_NearLimit() {
        Expense expense = new Expense();
        expense.setAmount(new BigDecimal("4200.00")); // 84% of 5000
        expense.setCategory("Food");
        expense.setType(ExpenseType.EXPENSE);
        expense.setDate(LocalDate.of(2026, 8, 10));

        when(budgetRepository.findByUserIdAndPeriod(1L, "2026-08")).thenReturn(List.of(sampleBudget));
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(expense));

        List<BudgetStatusResponse> statusList = budgetService.getBudgetStatus(1L, "2026-08");

        assertEquals(1, statusList.size());
        assertEquals(BudgetStatus.NEAR_LIMIT, statusList.get(0).getStatus());
    }

    @Test
    void getBudgetStatus_Exceeded() {
        Expense expense = new Expense();
        expense.setAmount(new BigDecimal("5500.00")); // 110% of 5000
        expense.setCategory("Food");
        expense.setType(ExpenseType.EXPENSE);
        expense.setDate(LocalDate.of(2026, 8, 10));

        when(budgetRepository.findByUserIdAndPeriod(1L, "2026-08")).thenReturn(List.of(sampleBudget));
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(expense));

        List<BudgetStatusResponse> statusList = budgetService.getBudgetStatus(1L, "2026-08");

        assertEquals(1, statusList.size());
        assertEquals(BudgetStatus.EXCEEDED, statusList.get(0).getStatus());
    }

    @Test
    void getBudgetStatus_ZeroLimit_HandlesSafely() {
        Budget zeroBudget = new Budget(sampleUser, "Food", BigDecimal.ZERO, "2026-08");
        zeroBudget.setId(11L);

        when(budgetRepository.findByUserIdAndPeriod(1L, "2026-08")).thenReturn(List.of(zeroBudget));
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        List<BudgetStatusResponse> statusList = budgetService.getBudgetStatus(1L, "2026-08");

        assertEquals(1, statusList.size());
        assertEquals(BigDecimal.ZERO, statusList.get(0).getPercentageUsed());
    }

    @Test
    void getBudgetStatus_InvalidPeriod_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> budgetService.getBudgetStatus(1L, "2026/08"));
    }
}
