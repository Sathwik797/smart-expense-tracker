package com.sathwik.expensetracker.repository;

import com.sathwik.expensetracker.entity.Expense;
import com.sathwik.expensetracker.entity.User;
import com.sathwik.expensetracker.enums.ExpenseType;
import com.sathwik.expensetracker.enums.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = new User(null, "Test User", "test@example.com");
        savedUser = userRepository.save(user);

        Expense e1 = new Expense();
        e1.setUser(savedUser);
        e1.setAmount(new BigDecimal("100.00"));
        e1.setCategory("Food");
        e1.setType(ExpenseType.EXPENSE);
        e1.setPaymentMethod(PaymentMethod.UPI);
        e1.setDate(LocalDate.now());
        expenseRepository.save(e1);

        Expense e2 = new Expense();
        e2.setUser(savedUser);
        e2.setAmount(new BigDecimal("200.00"));
        e2.setCategory("Food");
        e2.setType(ExpenseType.EXPENSE);
        e2.setPaymentMethod(PaymentMethod.CASH);
        e2.setDate(LocalDate.now());
        expenseRepository.save(e2);

        Expense e3 = new Expense();
        e3.setUser(savedUser);
        e3.setAmount(new BigDecimal("1000.00"));
        e3.setCategory("Salary");
        e3.setType(ExpenseType.INCOME);
        e3.setPaymentMethod(PaymentMethod.NET_BANKING);
        e3.setDate(LocalDate.now());
        expenseRepository.save(e3);
    }

    @Test
    void testSumAmountByUserIdAndType() {
        BigDecimal totalExpenses = expenseRepository.sumAmountByUserIdAndType(savedUser.getId(), ExpenseType.EXPENSE);
        BigDecimal totalIncome = expenseRepository.sumAmountByUserIdAndType(savedUser.getId(), ExpenseType.INCOME);

        assertNotNull(totalExpenses);
        assertEquals(0, new BigDecimal("300.00").compareTo(totalExpenses));

        assertNotNull(totalIncome);
        assertEquals(0, new BigDecimal("1000.00").compareTo(totalIncome));
    }

    @Test
    void testFindCategoryTotalsByUserIdAndType() {
        List<Object[]> categoryTotals = expenseRepository.findCategoryTotalsByUserIdAndType(savedUser.getId(), ExpenseType.EXPENSE);

        assertNotNull(categoryTotals);
        assertEquals(1, categoryTotals.size());
        assertEquals("Food", categoryTotals.get(0)[0]);
        assertEquals(0, new BigDecimal("300.00").compareTo((BigDecimal) categoryTotals.get(0)[1]));
    }

    @Test
    void testFindByUserIdOrderByDateDesc() {
        List<Expense> list = expenseRepository.findByUserIdOrderByDateDesc(savedUser.getId());

        assertEquals(3, list.size());
    }
}
