package com.sathwik.expensetracker.service;

import com.sathwik.expensetracker.dto.ImportSummaryResponse;
import com.sathwik.expensetracker.entity.Expense;
import com.sathwik.expensetracker.entity.User;
import com.sathwik.expensetracker.repository.ExpenseRepository;
import com.sathwik.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CsvImportService csvImportService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Demo User", "user1@example.com");
    }

    @Test
    void importCsv_ValidCsv_PersistsAllRecords() {
        String csvContent = "date,description,category,amount,paymentMethod,type\n" +
                "2026-08-01,Restaurant,Food,450.00,UPI,EXPENSE\n" +
                "2026-08-03,Metro Pass,Transport,80.00,UPI,EXPENSE\n" +
                "2026-08-04,Monthly Salary,Salary,50000.00,NET_BANKING,INCOME\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(expenseRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        ImportSummaryResponse summary = csvImportService.importCsv(file, 1L);

        assertNotNull(summary);
        assertEquals(3, summary.getTotalRows());
        assertEquals(3, summary.getSuccessfulRows());
        assertEquals(0, summary.getFailedRows());
        assertTrue(summary.getErrors().isEmpty());
        verify(expenseRepository, times(1)).saveAll(anyList());
    }

    @Test
    void importCsv_InvalidExtension_ThrowsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.txt", "text/plain", "content".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(IllegalArgumentException.class, () -> csvImportService.importCsv(file, 1L));
    }

    @Test
    void importCsv_EmptyFile_ThrowsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.csv", "text/csv", new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> csvImportService.importCsv(file, 1L));
    }

    @Test
    void importCsv_MissingHeaders_ThrowsIllegalArgumentException() {
        String csvContent = "wrong,headers,here\n2026-08-01,Desc,Food,100,UPI,EXPENSE\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(IllegalArgumentException.class, () -> csvImportService.importCsv(file, 1L));
    }

    @Test
    void importCsv_InvalidDateFormat_ReturnsRowErrorAndRollback() {
        String csvContent = "date,description,category,amount,paymentMethod,type\n" +
                "01/08/2026,Restaurant,Food,450.00,UPI,EXPENSE\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        ImportSummaryResponse summary = csvImportService.importCsv(file, 1L);

        assertEquals(1, summary.getTotalRows());
        assertEquals(0, summary.getSuccessfulRows());
        assertEquals(1, summary.getFailedRows());
        assertEquals(1, summary.getErrors().size());
        assertEquals(2, summary.getErrors().get(0).getRow());
        verify(expenseRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_NegativeAmount_ReturnsRowErrorAndRollback() {
        String csvContent = "date,description,category,amount,paymentMethod,type\n" +
                "2026-08-01,Restaurant,Food,-100.00,UPI,EXPENSE\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        ImportSummaryResponse summary = csvImportService.importCsv(file, 1L);

        assertEquals(0, summary.getSuccessfulRows());
        assertEquals(1, summary.getFailedRows());
        verify(expenseRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_InvalidEnum_ReturnsRowErrorAndRollback() {
        String csvContent = "date,description,category,amount,paymentMethod,type\n" +
                "2026-08-01,Restaurant,Food,100.00,INVALID_METHOD,EXPENSE\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        ImportSummaryResponse summary = csvImportService.importCsv(file, 1L);

        assertEquals(0, summary.getSuccessfulRows());
        assertEquals(1, summary.getFailedRows());
        verify(expenseRepository, never()).saveAll(anyList());
    }

    @Test
    void importCsv_MixedValidAndInvalidRows_AtomicRollbackNoPersistence() {
        String csvContent = "date,description,category,amount,paymentMethod,type\n" +
                "2026-08-01,Valid Row,Food,100.00,UPI,EXPENSE\n" +
                "2026-08-02,Invalid Row,Food,INVALID_AMOUNT,UPI,EXPENSE\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "expenses.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        ImportSummaryResponse summary = csvImportService.importCsv(file, 1L);

        assertEquals(2, summary.getTotalRows());
        assertEquals(0, summary.getSuccessfulRows());
        assertEquals(1, summary.getFailedRows());
        assertEquals(3, summary.getErrors().get(0).getRow());
        verify(expenseRepository, never()).saveAll(anyList());
    }
}
