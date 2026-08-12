package com.sathwik.expensetracker.service;

import com.sathwik.expensetracker.dto.ImportRowError;
import com.sathwik.expensetracker.dto.ImportSummaryResponse;
import com.sathwik.expensetracker.entity.Expense;
import com.sathwik.expensetracker.entity.User;
import com.sathwik.expensetracker.enums.ExpenseType;
import com.sathwik.expensetracker.enums.PaymentMethod;
import com.sathwik.expensetracker.repository.ExpenseRepository;
import com.sathwik.expensetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvImportService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public CsvImportService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ImportSummaryResponse importCsv(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded CSV file cannot be empty");
        }

        String filename = file.getOriginalFilename();
        if (filename != null && !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Invalid file format. Please upload a .csv file");
        }

        User user = userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User();
            newUser.setFullName("Demo User");
            newUser.setEmail("user" + userId + "@example.com");
            return userRepository.save(newUser);
        });

        List<ImportRowError> errors = new ArrayList<>();
        List<Expense> validExpenses = new ArrayList<>();
        int dataRowCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty or missing headers");
            }

            validateHeaders(headerLine);

            String line;
            int lineNumber = 1; // Header is row 1, data starts at row 2

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                dataRowCount++;
                String[] columns = line.split(",", -1);

                if (columns.length < 6) {
                    errors.add(new ImportRowError(lineNumber, "Row has insufficient columns (expected 6 columns)"));
                    continue;
                }

                String dateStr = columns[0].trim();
                String descStr = columns[1].trim();
                String categoryStr = columns[2].trim();
                String amountStr = columns[3].trim();
                String methodStr = columns[4].trim();
                String typeStr = columns[5].trim();

                // Validate Date
                LocalDate date = null;
                if (dateStr.isEmpty()) {
                    errors.add(new ImportRowError(lineNumber, "Date is required"));
                } else {
                    try {
                        date = LocalDate.parse(dateStr);
                    } catch (DateTimeParseException ex) {
                        errors.add(new ImportRowError(lineNumber, "Invalid date format (expected YYYY-MM-DD): " + dateStr));
                    }
                }

                // Validate Category
                if (categoryStr.isEmpty()) {
                    errors.add(new ImportRowError(lineNumber, "Category is required"));
                }

                // Validate Amount
                BigDecimal amount = null;
                if (amountStr.isEmpty()) {
                    errors.add(new ImportRowError(lineNumber, "Amount is required"));
                } else {
                    try {
                        amount = new BigDecimal(amountStr);
                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                            errors.add(new ImportRowError(lineNumber, "Amount must be greater than zero"));
                        }
                    } catch (NumberFormatException ex) {
                        errors.add(new ImportRowError(lineNumber, "Invalid monetary amount: " + amountStr));
                    }
                }

                // Validate PaymentMethod
                PaymentMethod paymentMethod = null;
                if (methodStr.isEmpty()) {
                    errors.add(new ImportRowError(lineNumber, "Payment method is required"));
                } else {
                    try {
                        paymentMethod = PaymentMethod.valueOf(methodStr.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        errors.add(new ImportRowError(lineNumber, "Invalid payment method: " + methodStr));
                    }
                }

                // Validate Type
                ExpenseType type = null;
                if (typeStr.isEmpty()) {
                    errors.add(new ImportRowError(lineNumber, "Transaction type is required"));
                } else {
                    try {
                        type = ExpenseType.valueOf(typeStr.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        errors.add(new ImportRowError(lineNumber, "Invalid transaction type: " + typeStr));
                    }
                }

                final int currentRow = lineNumber;
                // If this row has no errors, stage for batch save
                if (errors.stream().noneMatch(e -> e.getRow() == currentRow)) {
                    Expense expense = new Expense();
                    expense.setUser(user);
                    expense.setDate(date);
                    expense.setDescription(descStr);
                    expense.setCategory(categoryStr);
                    expense.setAmount(amount);
                    expense.setPaymentMethod(paymentMethod);
                    expense.setType(type);
                    validExpenses.add(expense);
                }
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to read CSV file: " + ex.getMessage());
        }

        if (dataRowCount == 0) {
            throw new IllegalArgumentException("CSV file contains no data rows");
        }

        // Atomic Rollback Strategy: If any row failed validation, do NOT save any row!
        if (!errors.isEmpty()) {
            return new ImportSummaryResponse(dataRowCount, 0, errors.size(), errors);
        }

        // All rows valid -> persist entire batch
        expenseRepository.saveAll(validExpenses);
        return new ImportSummaryResponse(dataRowCount, validExpenses.size(), 0, new ArrayList<>());
    }

    private void validateHeaders(String headerLine) {
        String[] headers = headerLine.split(",", -1);
        if (headers.length < 6) {
            throw new IllegalArgumentException("Missing required CSV headers. Expected: date,description,category,amount,paymentMethod,type");
        }

        String h0 = headers[0].trim().toLowerCase();
        String h1 = headers[1].trim().toLowerCase();
        String h2 = headers[2].trim().toLowerCase();
        String h3 = headers[3].trim().toLowerCase();
        String h4 = headers[4].trim().toLowerCase();
        String h5 = headers[5].trim().toLowerCase();

        if (!h0.equals("date") || !h1.equals("description") || !h2.equals("category") ||
                !h3.equals("amount") || !h4.equals("paymentmethod") || !h5.equals("type")) {
            throw new IllegalArgumentException("Invalid CSV headers. Expected: date,description,category,amount,paymentMethod,type");
        }
    }
}
