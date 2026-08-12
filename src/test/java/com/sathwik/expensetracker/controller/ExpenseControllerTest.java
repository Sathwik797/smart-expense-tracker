package com.sathwik.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sathwik.expensetracker.dto.DashboardResponse;
import com.sathwik.expensetracker.dto.ExpenseRequest;
import com.sathwik.expensetracker.dto.ExpenseResponse;
import com.sathwik.expensetracker.enums.ExpenseType;
import com.sathwik.expensetracker.enums.PaymentMethod;
import com.sathwik.expensetracker.exception.ResourceNotFoundException;
import com.sathwik.expensetracker.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @Test
    void addExpense_ValidRequest_Returns201Created() throws Exception {
        ExpenseRequest request = new ExpenseRequest(
                1L,
                new BigDecimal("100.00"),
                "Food",
                ExpenseType.EXPENSE,
                PaymentMethod.UPI,
                "Dinner",
                LocalDate.now()
        );

        ExpenseResponse response = new ExpenseResponse();
        response.setId(1L);
        response.setUserId(1L);
        response.setAmount(new BigDecimal("100.00"));
        response.setCategory("Food");
        response.setType(ExpenseType.EXPENSE);
        response.setPaymentMethod(PaymentMethod.UPI);
        response.setDate(LocalDate.now());

        when(expenseService.addExpense(any(ExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    void addExpense_InvalidRequest_Returns400BadRequest() throws Exception {
        ExpenseRequest invalidRequest = new ExpenseRequest(
                null, // Missing user ID
                new BigDecimal("-50.00"), // Negative amount
                "", // Blank category
                null,
                null,
                "",
                null
        );

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.amount").exists())
                .andExpect(jsonPath("$.fieldErrors.category").exists());
    }

    @Test
    void getUserExpenses_Returns200Ok() throws Exception {
        when(expenseService.getUserExpenses(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/expenses/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getExpenseById_Success_Returns200Ok() throws Exception {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(5L);
        response.setAmount(new BigDecimal("50.00"));
        response.setCategory("Travel");

        when(expenseService.getExpenseById(5L)).thenReturn(response);

        mockMvc.perform(get("/api/expenses/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.category").value("Travel"));
    }

    @Test
    void getExpenseById_NotFound_Returns404NotFound() throws Exception {
        when(expenseService.getExpenseById(99L))
                .thenThrow(new ResourceNotFoundException("Expense not found with ID: 99"));

        mockMvc.perform(get("/api/expenses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Expense not found with ID: 99"));
    }

    @Test
    void updateExpense_Success_Returns200Ok() throws Exception {
        ExpenseRequest request = new ExpenseRequest(
                1L,
                new BigDecimal("250.00"),
                "Shopping",
                ExpenseType.EXPENSE,
                PaymentMethod.CREDIT_CARD,
                "Clothes",
                LocalDate.now()
        );

        ExpenseResponse response = new ExpenseResponse();
        response.setId(10L);
        response.setAmount(new BigDecimal("250.00"));

        when(expenseService.updateExpense(eq(10L), any(ExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/expenses/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void deleteExpense_Success_Returns204NoContent() throws Exception {
        doNothing().when(expenseService).deleteExpense(10L);

        mockMvc.perform(delete("/api/expenses/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getDashboard_Returns200Ok() throws Exception {
        DashboardResponse dashboard = new DashboardResponse(
                new BigDecimal("500.00"),
                new BigDecimal("2000.00"),
                Collections.emptyMap(),
                "Food",
                Collections.emptyMap(),
                "Healthy balance"
        );

        when(expenseService.getDashboard(1L)).thenReturn(dashboard);

        mockMvc.perform(get("/api/expenses/dashboard/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpense").value(500.00))
                .andExpect(jsonPath("$.totalIncome").value(2000.00))
                .andExpect(jsonPath("$.topCategory").value("Food"));
    }
}
