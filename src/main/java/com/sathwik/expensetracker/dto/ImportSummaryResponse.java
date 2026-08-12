package com.sathwik.expensetracker.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportSummaryResponse {

    private int totalRows;
    private int successfulRows;
    private int failedRows;
    private List<ImportRowError> errors = new ArrayList<>();

    public ImportSummaryResponse() {
    }

    public ImportSummaryResponse(int totalRows, int successfulRows, int failedRows, List<ImportRowError> errors) {
        this.totalRows = totalRows;
        this.successfulRows = successfulRows;
        this.failedRows = failedRows;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessfulRows() {
        return successfulRows;
    }

    public void setSuccessfulRows(int successfulRows) {
        this.successfulRows = successfulRows;
    }

    public int getFailedRows() {
        return failedRows;
    }

    public void setFailedRows(int failedRows) {
        this.failedRows = failedRows;
    }

    public List<ImportRowError> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportRowError> errors) {
        this.errors = errors;
    }
}
