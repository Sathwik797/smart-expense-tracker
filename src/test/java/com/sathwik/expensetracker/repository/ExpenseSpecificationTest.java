package com.sathwik.expensetracker.repository;

import com.sathwik.expensetracker.dto.ExpenseSearchRequest;
import com.sathwik.expensetracker.entity.Expense;
import com.sathwik.expensetracker.entity.User;
import com.sathwik.expensetracker.enums.ExpenseType;
import com.sathwik.expensetracker.enums.PaymentMethod;
import com.sathwik.expensetracker.repository.specification.ExpenseSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ExpenseSpecificationTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User(null, "Search User", "search@example.com"));

        Expense e1 = new Expense();
        e1.setUser(testUser);
        e1.setAmount(new BigDecimal("150.00"));
        e1.setCategory("Food");
        e1.setType(ExpenseType.EXPENSE);
        e1.setPaymentMethod(PaymentMethod.UPI);
        e1.setDescription("Restaurant dinner");
        e1.setDate(LocalDate.of(2026, 8, 1));
        expenseRepository.save(e1);

        Expense e2 = new Expense();
        e2.setUser(testUser);
        e2.setAmount(new BigDecimal("500.00"));
        e2.setCategory("Travel");
        e2.setType(ExpenseType.EXPENSE);
        e2.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        e2.setDescription("Flight ticket booking");
        e2.setDate(LocalDate.of(2026, 8, 5));
        expenseRepository.save(e2);

        Expense e3 = new Expense();
        e3.setUser(testUser);
        e3.setAmount(new BigDecimal("2000.00"));
        e3.setCategory("Salary");
        e3.setType(ExpenseType.INCOME);
        e3.setPaymentMethod(PaymentMethod.NET_BANKING);
        e3.setDescription("Monthly salary credit");
        e3.setDate(LocalDate.of(2026, 8, 10));
        expenseRepository.save(e3);
    }

    @Test
    void searchByCategoryOnly() {
        ExpenseSearchRequest request = new ExpenseSearchRequest();
        request.setCategory("Food");

        Specification<Expense> spec = ExpenseSpecification.buildSpecification(testUser.getId(), request);
        List<Expense> results = expenseRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals("Food", results.get(0).getCategory());
        assertEquals(new BigDecimal("150.00"), results.get(0).getAmount());
    }

    @Test
    void searchByCategoryAndPaymentMethod() {
        ExpenseSearchRequest request = new ExpenseSearchRequest();
        request.setCategory("Travel");
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        Specification<Expense> spec = ExpenseSpecification.buildSpecification(testUser.getId(), request);
        List<Expense> results = expenseRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals("Travel", results.get(0).getCategory());
        assertEquals(PaymentMethod.CREDIT_CARD, results.get(0).getPaymentMethod());
    }

    @Test
    void searchByDateRange() {
        ExpenseSearchRequest request = new ExpenseSearchRequest();
        request.setStartDate(LocalDate.of(2026, 8, 2));
        request.setEndDate(LocalDate.of(2026, 8, 11));

        Specification<Expense> spec = ExpenseSpecification.buildSpecification(testUser.getId(), request);
        List<Expense> results = expenseRepository.findAll(spec);

        assertEquals(2, results.size());
    }

    @Test
    void searchByMinAndMaxAmount() {
        ExpenseSearchRequest request = new ExpenseSearchRequest();
        request.setMinAmount(new BigDecimal("200.00"));
        request.setMaxAmount(new BigDecimal("600.00"));

        Specification<Expense> spec = ExpenseSpecification.buildSpecification(testUser.getId(), request);
        List<Expense> results = expenseRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals("Travel", results.get(0).getCategory());
    }

    @Test
    void searchByKeyword() {
        ExpenseSearchRequest request = new ExpenseSearchRequest();
        request.setKeyword("dinner");

        Specification<Expense> spec = ExpenseSpecification.buildSpecification(testUser.getId(), request);
        List<Expense> results = expenseRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals("Restaurant dinner", results.get(0).getDescription());
    }

    @Test
    void searchWithNoMatches_ReturnsEmptyList() {
        ExpenseSearchRequest request = new ExpenseSearchRequest();
        request.setCategory("NonExistentCategory");

        Specification<Expense> spec = ExpenseSpecification.buildSpecification(testUser.getId(), request);
        List<Expense> results = expenseRepository.findAll(spec);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchWithInvalidAmountRange_ThrowsIllegalArgumentException() {
        ExpenseSearchRequest request = new ExpenseSearchRequest();
        request.setMinAmount(new BigDecimal("500.00"));
        request.setMaxAmount(new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, request::validate);
    }

    @Test
    void searchWithInvalidDateRange_ThrowsIllegalArgumentException() {
        ExpenseSearchRequest request = new ExpenseSearchRequest();
        request.setStartDate(LocalDate.of(2026, 8, 15));
        request.setEndDate(LocalDate.of(2026, 8, 10));

        assertThrows(IllegalArgumentException.class, request::validate);
    }
}
