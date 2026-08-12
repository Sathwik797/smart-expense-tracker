package com.sathwik.expensetracker.controller;

import com.sathwik.expensetracker.dto.ImportSummaryResponse;
import com.sathwik.expensetracker.service.CsvImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseImportController {

    private final CsvImportService csvImportService;

    public ExpenseImportController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @PostMapping("/import")
    public ResponseEntity<ImportSummaryResponse> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", defaultValue = "1") Long userId) {
        ImportSummaryResponse summary = csvImportService.importCsv(file, userId);
        if (summary.getFailedRows() > 0) {
            return new ResponseEntity<>(summary, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(summary);
    }
}
