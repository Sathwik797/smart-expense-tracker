package com.sathwik.expensetracker.service;

import com.sathwik.expensetracker.dto.DashboardResponse;
import com.sathwik.expensetracker.dto.ExpenseRequest;
import com.sathwik.expensetracker.dto.ExpenseResponse;
import com.sathwik.expensetracker.entity.Expense;
import com.sathwik.expensetracker.entity.User;
import com.sathwik.expensetracker.enums.ExpenseType;
import com.sathwik.expensetracker.enums.PaymentMethod;
import com.sathwik.expensetracker.exception.ResourceNotFoundException;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User sampleUser;
    private Expense sampleExpense;
    private ExpenseRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Demo User", "user1@example.com");

        sampleExpense = new Expense();
        sampleExpense.setId(100L);
        sampleExpense.setUser(sampleUser);
        sampleExpense.setAmount(new BigDecimal("150.50"));
        sampleExpense.setCategory("Food");
        sampleExpense.setType(ExpenseType.EXPENSE);
        sampleExpense.setPaymentMethod(PaymentMethod.UPI);
        sampleExpense.setDescription("Lunch");
        sampleExpense.setDate(LocalDate.now());

        sampleRequest = new ExpenseRequest(
                1L,
                new BigDecimal("150.50"),
                "Food",
                ExpenseType.EXPENSE,
                PaymentMethod.UPI,
                "Lunch",
                LocalDate.now()
        );
    }

    @Test
    void addExpense_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);

        ExpenseResponse response = expenseService.addExpense(sampleRequest);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(new BigDecimal("150.50"), response.getAmount());
        assertEquals("Food", response.getCategory());
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void getExpenseById_Success() {
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(sampleExpense));

        ExpenseResponse response = expenseService.getExpenseById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Food", response.getCategory());
    }

    @Test
    void getExpenseById_NotFound_ThrowsResourceNotFoundException() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.getExpenseById(999L));
    }

    @Test
    void getUserExpenses_Success() {
        when(expenseRepository.findByUserIdOrderByDateDesc(1L)).thenReturn(List.of(sampleExpense));

        List<ExpenseResponse> expenses = expenseService.getUserExpenses(1L);

        assertEquals(1, expenses.size());
        assertEquals("Food", expenses.get(0).getCategory());
    }

    @Test
    void updateExpense_Success() {
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(sampleExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);

        ExpenseRequest updateRequest = new ExpenseRequest(
                1L,
                new BigDecimal("200.00"),
                "Groceries",
                ExpenseType.EXPENSE,
                PaymentMethod.CREDIT_CARD,
                "Weekly Shopping",
                LocalDate.now()
        );

        ExpenseResponse response = expenseService.updateExpense(100L, updateRequest);

        assertNotNull(response);
        verify(expenseRepository, times(1)).save(sampleExpense);
    }

    @Test
    void deleteExpense_Success() {
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(sampleExpense));
        doNothing().when(expenseRepository).delete(sampleExpense);

        expenseService.deleteExpense(100L);

        verify(expenseRepository, times(1)).delete(sampleExpense);
    }

    @Test
    void deleteExpense_NotFound_ThrowsException() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.deleteExpense(999L));
    }

    @Test
    void getTotalExpense_ReturnsSum() {
        when(expenseRepository.sumAmountByUserIdAndType(1L, ExpenseType.EXPENSE))
                .thenReturn(new BigDecimal("450.00"));

        BigDecimal total = expenseService.getTotalExpense(1L);

        assertEquals(new BigDecimal("450.00"), total);
    }

    @Test
    void getTotalExpense_WhenNull_ReturnsZero() {
        when(expenseRepository.sumAmountByUserIdAndType(1L, ExpenseType.EXPENSE)).thenReturn(null);

        BigDecimal total = expenseService.getTotalExpense(1L);

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void getCategoryWise_ReturnsMap() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Food", new BigDecimal("150.00")});
        rows.add(new Object[]{"Travel", new BigDecimal("100.00")});

        when(expenseRepository.findCategoryTotalsByUserIdAndType(1L, ExpenseType.EXPENSE)).thenReturn(rows);

        Map<String, BigDecimal> result = expenseService.getCategoryWise(1L);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("150.00"), result.get("Food"));
    }

    @Test
    void getTopCategory_ReturnsCategoryWithMaxSpending() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Food", new BigDecimal("150.00")});
        rows.add(new Object[]{"Shopping", new BigDecimal("500.00")});

        when(expenseRepository.findCategoryTotalsByUserIdAndType(1L, ExpenseType.EXPENSE)).thenReturn(rows);

        String topCategory = expenseService.getTopCategory(1L);

        assertEquals("Shopping", topCategory);
    }

    @Test
    void getDashboard_ReturnsAggregatedData() {
        when(expenseRepository.sumAmountByUserIdAndType(1L, ExpenseType.EXPENSE)).thenReturn(new BigDecimal("200.00"));
        when(expenseRepository.sumAmountByUserIdAndType(1L, ExpenseType.INCOME)).thenReturn(new BigDecimal("1000.00"));
        when(expenseRepository.findCategoryTotalsByUserIdAndType(1L, ExpenseType.EXPENSE)).thenReturn(Collections.emptyList());
        when(expenseRepository.findByUserIdAndType(1L, ExpenseType.EXPENSE)).thenReturn(Collections.emptyList());

        DashboardResponse dashboard = expenseService.getDashboard(1L);

        assertNotNull(dashboard);
        assertEquals(new BigDecimal("200.00"), dashboard.getTotalExpense());
        assertEquals(new BigDecimal("1000.00"), dashboard.getTotalIncome());
        assertNotNull(dashboard.getInsight());
    }
}
