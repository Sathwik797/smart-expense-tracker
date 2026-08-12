package com.sathwik.expensetracker.controller;

import com.sathwik.expensetracker.dto.ImportSummaryResponse;
import com.sathwik.expensetracker.service.CsvImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseImportController.class)
class ExpenseImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CsvImportService csvImportService;

    @Test
    void importCsv_Success_Returns200Ok() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.csv", "text/csv", "header\ndata".getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse summary = new ImportSummaryResponse(2, 2, 0, Collections.emptyList());

        when(csvImportService.importCsv(any(), eq(1L))).thenReturn(summary);

        mockMvc.perform(multipart("/api/expenses/import")
                        .file(file)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulRows").value(2))
                .andExpect(jsonPath("$.failedRows").value(0));
    }

    @Test
    void importCsv_ValidationErrors_Returns400BadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.csv", "text/csv", "header\ninvalid".getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse summary = new ImportSummaryResponse(1, 0, 1, Collections.emptyList());

        when(csvImportService.importCsv(any(), eq(1L))).thenReturn(summary);

        mockMvc.perform(multipart("/api/expenses/import")
                        .file(file)
                        .param("userId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.successfulRows").value(0))
                .andExpect(jsonPath("$.failedRows").value(1));
    }
}
