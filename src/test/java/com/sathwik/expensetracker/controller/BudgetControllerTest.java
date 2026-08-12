package com.sathwik.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sathwik.expensetracker.dto.BudgetRequest;
import com.sathwik.expensetracker.dto.BudgetResponse;
import com.sathwik.expensetracker.dto.BudgetStatusResponse;
import com.sathwik.expensetracker.enums.BudgetStatus;
import com.sathwik.expensetracker.exception.ResourceNotFoundException;
import com.sathwik.expensetracker.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BudgetService budgetService;

    @Test
    void createBudget_ValidRequest_Returns21Created() throws Exception {
        BudgetRequest request = new BudgetRequest(1L, "Food", new BigDecimal("5000.00"), "2026-08");

        BudgetResponse response = new BudgetResponse();
        response.setId(10L);
        response.setUserId(1L);
        response.setCategory("Food");
        response.setMonthlyLimit(new BigDecimal("5000.00"));
        response.setPeriod("2026-08");

        when(budgetService.createBudget(any(BudgetRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    void createBudget_InvalidRequest_Returns400BadRequest() throws Exception {
        BudgetRequest invalidRequest = new BudgetRequest(null, "", new BigDecimal("-100"), "2026/08");

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getUserBudgets_Returns200Ok() throws Exception {
        when(budgetService.getUserBudgets(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/budgets/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateBudget_Success_Returns200Ok() throws Exception {
        BudgetRequest request = new BudgetRequest(1L, "Food", new BigDecimal("6000.00"), "2026-08");

        BudgetResponse response = new BudgetResponse();
        response.setId(10L);
        response.setMonthlyLimit(new BigDecimal("6000.00"));

        when(budgetService.updateBudget(eq(10L), any(BudgetRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/budgets/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void deleteBudget_Success_Returns204NoContent() throws Exception {
        doNothing().when(budgetService).deleteBudget(10L);

        mockMvc.perform(delete("/api/budgets/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getBudgetStatus_Returns200Ok() throws Exception {
        BudgetStatusResponse statusResponse = new BudgetStatusResponse(
                10L, 1L, "Food", "2026-08",
                new BigDecimal("5000.00"), new BigDecimal("2000.00"),
                new BigDecimal("3000.00"), new BigDecimal("40.00"),
                BudgetStatus.UNDER_BUDGET
        );

        when(budgetService.getBudgetStatus(1L, "2026-08")).thenReturn(List.of(statusResponse));

        mockMvc.perform(get("/api/budgets/user/1/status?period=2026-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Food"))
                .andExpect(jsonPath("$[0].status").value("UNDER_BUDGET"));
    }
}
