const USER_ID = 1;
let categoryChart = null;
let trendChart = null;
let currentExpenses = [];
let currentBudgets = [];

document.addEventListener("DOMContentLoaded", () => {
    displayCurrentDate();
    setDefaultDate();
    setDefaultBudgetPeriod();
    loadDashboard();
    loadExpenses();
    loadBudgets();
});

// Header Date Display
function displayCurrentDate() {
    const dateDisplay = document.getElementById("current-date-display");
    if (dateDisplay) {
        const options = { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' };
        dateDisplay.innerText = new Date().toLocaleDateString("en-US", options);
    }
}

// Default date pickers
function setDefaultDate() {
    const dateInput = document.getElementById("date");
    if (dateInput && !dateInput.value) {
        dateInput.value = new Date().toISOString().split("T")[0];
    }
}

function setDefaultBudgetPeriod() {
    const periodInput = document.getElementById("budget-period-input");
    if (periodInput && !periodInput.value) {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        periodInput.value = `${year}-${month}`;
    }
    const modalPeriod = document.getElementById("budget-period");
    if (modalPeriod && !modalPeriod.value) {
        const now = new Date();
        modalPeriod.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    }
}

// Toast Alert
function showToast(message, type = "success") {
    const container = document.getElementById("toast-container");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    toast.innerText = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3500);
}

// Load Dashboard Overview Metrics
function loadDashboard() {
    fetch(`/api/expenses/dashboard/${USER_ID}`)
        .then(res => {
            if (!res.ok) throw new Error("Failed to load dashboard statistics");
            return res.json();
        })
        .then(data => {
            const totalExpense = parseFloat(data.totalExpense || 0);
            const totalIncome = parseFloat(data.totalIncome || 0);
            const netBalance = totalIncome - totalExpense;

            document.getElementById("stat-total-expense").innerText = `₹${formatCurrency(totalExpense)}`;
            document.getElementById("stat-total-income").innerText = `₹${formatCurrency(totalIncome)}`;
            document.getElementById("stat-net-balance").innerText = `₹${formatCurrency(netBalance)}`;
            document.getElementById("stat-top-category").innerText = data.topCategory || "N/A";
            document.getElementById("insight-text").innerText = data.insight || "No financial data available yet.";

            const balancePill = document.getElementById("balance-pill");
            if (balancePill) {
                if (netBalance >= 0) {
                    balancePill.className = "metric-pill pill-blue";
                    balancePill.innerText = "Surplus";
                } else {
                    balancePill.className = "metric-pill pill-red";
                    balancePill.innerText = "Deficit";
                }
            }

            renderCategoryChart(data.categoryWise || {});
            renderTrendChart(data.dailySpending || {});
        })
        .catch(err => {
            console.error("Dashboard load error:", err);
            showToast("Could not load dashboard data.", "error");
        });
}

// Load Expenses List
function loadExpenses() {
    fetch(`/api/expenses/user/${USER_ID}`)
        .then(res => {
            if (!res.ok) throw new Error("Failed to fetch transactions");
            return res.json();
        })
        .then(expenses => {
            currentExpenses = expenses;
            renderExpenseTable(expenses);
        })
        .catch(err => {
            console.error("Load expenses error:", err);
            const tbody = document.getElementById("expense-table-body");
            if (tbody) {
                tbody.innerHTML = `<tr><td colspan="7" class="empty-state">Error loading transactions.</td></tr>`;
            }
        });
}

// Advanced Search via JPA Specifications API
function handleAdvancedSearch(event) {
    event.preventDefault();

    const keyword = document.getElementById("search-keyword").value.trim();
    const category = document.getElementById("search-category").value.trim();
    const type = document.getElementById("search-type").value;
    const paymentMethod = document.getElementById("search-payment-method").value;
    const startDate = document.getElementById("search-start-date").value;
    const endDate = document.getElementById("search-end-date").value;
    const minAmount = document.getElementById("search-min-amount").value;
    const maxAmount = document.getElementById("search-max-amount").value;

    if (minAmount && maxAmount && parseFloat(minAmount) > parseFloat(maxAmount)) {
        showToast("Min amount cannot be greater than max amount.", "error");
        return;
    }

    if (startDate && endDate && startDate > endDate) {
        showToast("Start date cannot be after end date.", "error");
        return;
    }

    const params = new URLSearchParams();
    if (keyword) params.append("keyword", keyword);
    if (category) params.append("category", category);
    if (type) params.append("type", type);
    if (paymentMethod) params.append("paymentMethod", paymentMethod);
    if (startDate) params.append("startDate", startDate);
    if (endDate) params.append("endDate", endDate);
    if (minAmount) params.append("minAmount", minAmount);
    if (maxAmount) params.append("maxAmount", maxAmount);

    fetch(`/api/expenses/user/${USER_ID}/search?${params.toString()}`)
        .then(res => {
            if (!res.ok) throw new Error("Search request failed");
            return res.json();
        })
        .then(expenses => {
            currentExpenses = expenses;
            renderExpenseTable(expenses);
            showToast(`Found ${expenses.length} matching transactions.`, "success");
        })
        .catch(err => {
            console.error("Search error:", err);
            showToast("Search failed: " + err.message, "error");
        });
}

function resetAdvancedSearch() {
    document.getElementById("search-form").reset();
    loadExpenses();
}

// Load Category Budgets Status
function loadBudgets() {
    const periodInput = document.getElementById("budget-period-input");
    const period = periodInput ? periodInput.value : "";

    fetch(`/api/budgets/user/${USER_ID}/status?period=${period}`)
        .then(res => {
            if (!res.ok) throw new Error("Failed to load budget statuses");
            return res.json();
        })
        .then(budgets => {
            currentBudgets = budgets;
            renderBudgetGrid(budgets);
        })
        .catch(err => {
            console.error("Budget load error:", err);
        });
}

// Render Budget Progress Cards
function renderBudgetGrid(budgets) {
    const grid = document.getElementById("budget-status-grid");
    if (!grid) return;

    if (!budgets || budgets.length === 0) {
        grid.innerHTML = `<div class="empty-state">No budgets configured for this period. Click "+ Set Budget" to create one.</div>`;
        return;
    }

    grid.innerHTML = budgets.map(b => {
        let statusBadgeClass = "pill-green";
        let fillClass = "bg-under";
        if (b.status === "NEAR_LIMIT") {
            statusBadgeClass = "pill-yellow";
            fillClass = "bg-near";
        } else if (b.status === "EXCEEDED") {
            statusBadgeClass = "pill-red";
            fillClass = "bg-exceeded";
        }

        const percentage = Math.min(parseFloat(b.percentageUsed || 0), 100);

        return `
            <div class="budget-card-item">
                <div class="budget-card-header">
                    <span class="budget-category-title">${getCategoryIcon(b.category)} ${escapeHtml(b.category)}</span>
                    <span class="metric-pill ${statusBadgeClass}">${b.status.replace('_', ' ')}</span>
                </div>
                <div>
                    <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 0.25rem;">
                        <span>Spent: <strong>₹${formatCurrency(b.amountSpent)}</strong></span>
                        <span>Limit: ₹${formatCurrency(b.monthlyLimit)}</span>
                    </div>
                    <div class="budget-progress-bar-bg">
                        <div class="budget-progress-fill ${fillClass}" style="width: ${percentage}%;"></div>
                    </div>
                </div>
                <div class="budget-card-footer">
                    <span>${b.percentageUsed}% Used</span>
                    <span>Remaining: ₹${formatCurrency(b.remainingAmount)}</span>
                    <button class="btn-action-icon btn-delete-icon" onclick="deleteBudget(${b.id})" title="Delete Budget">🗑️</button>
                </div>
            </div>
        `;
    }).join("");
}

// Budget Modal Handlers
function openBudgetModal() {
    document.getElementById("budget-form").reset();
    document.getElementById("budget-id").value = "";
    setDefaultBudgetPeriod();
    document.getElementById("budget-modal").classList.remove("hidden");
}

function closeBudgetModal() {
    document.getElementById("budget-modal").classList.add("hidden");
}

function handleBudgetSubmit(event) {
    event.preventDefault();

    const budgetId = document.getElementById("budget-id").value;
    const category = document.getElementById("budget-category").value.trim();
    const limit = parseFloat(document.getElementById("budget-limit").value);
    const period = document.getElementById("budget-period").value;

    if (!category || !limit || limit <= 0 || !period) {
        showToast("Please provide valid budget details.", "error");
        return;
    }

    const payload = {
        userId: USER_ID,
        category: category,
        monthlyLimit: limit,
        period: period
    };

    const isUpdate = Boolean(budgetId);
    const url = isUpdate ? `/api/budgets/${budgetId}` : "/api/budgets";
    const method = isUpdate ? "PUT" : "POST";

    fetch(url, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
    .then(async res => {
        if (!res.ok) {
            const errData = await res.json().catch(() => ({ message: "Failed to save budget" }));
            throw new Error(errData.message || "Failed to save budget");
        }
        return res.json();
    })
    .then(() => {
        showToast(isUpdate ? "Budget updated!" : "Budget set successfully!", "success");
        closeBudgetModal();
        loadBudgets();
    })
    .catch(err => {
        console.error("Budget save error:", err);
        showToast(err.message, "error");
    });
}

function deleteBudget(id) {
    if (!confirm("Are you sure you want to delete this category budget?")) return;

    fetch(`/api/budgets/${id}`, { method: "DELETE" })
        .then(res => {
            if (!res.ok && res.status !== 204) throw new Error("Failed to delete budget");
            showToast("Budget deleted!", "success");
            loadBudgets();
        })
        .catch(err => showToast(err.message, "error"));
}

// CSV Modal & Upload Handlers
function openCsvModal() {
    document.getElementById("csv-form").reset();
    const resultPanel = document.getElementById("csv-result-panel");
    if (resultPanel) {
        resultPanel.classList.add("hidden");
        resultPanel.innerHTML = "";
    }
    document.getElementById("csv-modal").classList.remove("hidden");
}

function closeCsvModal() {
    document.getElementById("csv-modal").classList.add("hidden");
}

function handleCsvUpload(event) {
    event.preventDefault();

    const fileInput = document.getElementById("csv-file-input");
    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
        showToast("Please select a .csv file.", "error");
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    formData.append("userId", USER_ID);

    const submitBtn = document.getElementById("csv-submit-btn");
    if (submitBtn) submitBtn.disabled = true;

    fetch("/api/expenses/import", {
        method: "POST",
        body: formData
    })
    .then(async res => {
        const data = await res.json();
        if (submitBtn) submitBtn.disabled = false;
        renderCsvResultPanel(data, res.ok);

        if (res.ok) {
            showToast(`Imported ${data.successfulRows} transactions successfully!`, "success");
            loadDashboard();
            loadExpenses();
            loadBudgets();
        } else {
            showToast(`Import failed. ${data.failedRows} errors found (0 rows saved).`, "error");
        }
    })
    .catch(err => {
        if (submitBtn) submitBtn.disabled = false;
        console.error("CSV upload error:", err);
        showToast("Failed to upload CSV file.", "error");
    });
}

function renderCsvResultPanel(data, isSuccess) {
    const panel = document.getElementById("csv-result-panel");
    if (!panel) return;

    panel.classList.remove("hidden");

    let statusHeader = isSuccess
        ? `<h4 style="color: #10b981;">✅ Import Successful (All ${data.successfulRows} rows saved)</h4>`
        : `<h4 style="color: #ef4444;">❌ Import Failed — Atomic Rollback (0 rows saved)</h4>`;

    let errorsTable = "";
    if (data.errors && data.errors.length > 0) {
        errorsTable = `
            <div style="max-height: 180px; overflow-y: auto; margin-top: 0.5rem;">
                <table class="saas-table" style="font-size: 0.8rem;">
                    <thead>
                        <tr><th>Row #</th><th>Validation Error</th></tr>
                    </thead>
                    <tbody>
                        ${data.errors.map(e => `
                            <tr>
                                <td><strong>Row ${e.row}</strong></td>
                                <td style="color: #dc2626;">${escapeHtml(e.message)}</td>
                            </tr>
                        `).join("")}
                    </tbody>
                </table>
            </div>
        `;
    }

    panel.innerHTML = `
        ${statusHeader}
        <div style="font-size: 0.85rem; margin-top: 0.35rem;">
            <span>Total Rows: ${data.totalRows} | </span>
            <span style="color: #16a34a;">Saved: ${data.successfulRows} | </span>
            <span style="color: #dc2626;">Failed: ${data.failedRows}</span>
        </div>
        ${errorsTable}
    `;
}

// Render Table
function renderExpenseTable(expenses) {
    const tbody = document.getElementById("expense-table-body");
    const countBadge = document.getElementById("results-count-badge");

    if (countBadge) {
        countBadge.innerText = `${expenses ? expenses.length : 0} records`;
    }

    if (!tbody) return;

    if (!expenses || expenses.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No transactions match the selected criteria.</td></tr>`;
        return;
    }

    tbody.innerHTML = expenses.map(exp => {
        const isExpense = exp.type === "EXPENSE";
        const typeClass = isExpense ? "type-expense" : "type-income";
        const amountPrefix = isExpense ? "-" : "+";
        const amountColor = isExpense ? "#ef4444" : "#10b981";

        return `
            <tr>
                <td><strong>${exp.date || ''}</strong></td>
                <td><span class="type-badge ${typeClass}">${exp.type}</span></td>
                <td>${getCategoryIcon(exp.category)} ${escapeHtml(exp.category)}</td>
                <td style="color: ${amountColor}; font-weight: 700;">${amountPrefix}₹${formatCurrency(exp.amount)}</td>
                <td><span class="method-tag">${escapeHtml(exp.paymentMethod)}</span></td>
                <td>${escapeHtml(exp.description || '—')}</td>
                <td class="text-right">
                    <button class="btn-action-icon btn-edit-icon" onclick="editExpense(${exp.id})" title="Edit">✏️ Edit</button>
                    <button class="btn-action-icon btn-delete-icon" onclick="deleteExpense(${exp.id})" title="Delete">🗑️ Delete</button>
                </td>
            </tr>
        `;
    }).join("");
}

// Category Icon
function getCategoryIcon(category) {
    if (!category) return "🏷️";
    const cat = category.toLowerCase();
    if (cat.includes("food") || cat.includes("dinner") || cat.includes("lunch")) return "🍕";
    if (cat.includes("travel") || cat.includes("flight") || cat.includes("cab")) return "✈️";
    if (cat.includes("shopping") || cat.includes("clothes")) return "🛍️";
    if (cat.includes("salary") || cat.includes("income")) return "💼";
    if (cat.includes("rent") || cat.includes("bill")) return "🏠";
    return "🏷️";
}

// Transaction Modal Handlers
function openTransactionModal() {
    resetForm();
    document.getElementById("transaction-modal").classList.remove("hidden");
}

function closeTransactionModal() {
    document.getElementById("transaction-modal").classList.add("hidden");
}

function closeModalOnBackdrop(event, modalId) {
    if (event.target.id === modalId) {
        document.getElementById(modalId).classList.add("hidden");
    }
}

function handleTransactionSubmit(event) {
    event.preventDefault();

    const expenseId = document.getElementById("expense-id").value;
    const amountVal = parseFloat(document.getElementById("amount").value);
    const categoryVal = document.getElementById("category").value.trim();
    const typeVal = document.getElementById("type").value;
    const paymentMethodVal = document.getElementById("paymentMethod").value;
    const dateVal = document.getElementById("date").value;
    const descVal = document.getElementById("description").value.trim();

    if (!amountVal || amountVal <= 0) {
        showToast("Please enter a positive amount.", "error");
        return;
    }

    const payload = {
        userId: USER_ID,
        amount: amountVal,
        category: categoryVal,
        type: typeVal,
        paymentMethod: paymentMethodVal,
        date: dateVal,
        description: descVal
    };

    const isUpdate = Boolean(expenseId);
    const url = isUpdate ? `/api/expenses/${expenseId}` : "/api/expenses";
    const method = isUpdate ? "PUT" : "POST";

    fetch(url, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
    .then(async res => {
        if (!res.ok) {
            const errData = await res.json().catch(() => ({ message: "Request failed" }));
            let msg = errData.message || "Operation failed.";
            if (errData.fieldErrors) {
                msg = Object.values(errData.fieldErrors).join(", ");
            }
            throw new Error(msg);
        }
        return res.json();
    })
    .then(() => {
        showToast(isUpdate ? "Transaction updated!" : "Transaction added!", "success");
        closeTransactionModal();
        loadDashboard();
        loadExpenses();
        loadBudgets();
    })
    .catch(err => showToast(err.message, "error"));
}

function editExpense(id) {
    const expense = currentExpenses.find(e => e.id === id);
    if (!expense) return;

    document.getElementById("expense-id").value = expense.id;
    document.getElementById("amount").value = expense.amount;
    document.getElementById("category").value = expense.category;
    document.getElementById("type").value = expense.type;
    document.getElementById("paymentMethod").value = expense.paymentMethod;
    document.getElementById("date").value = expense.date;
    document.getElementById("description").value = expense.description || "";

    document.getElementById("modal-title").innerText = "Edit Transaction";
    document.getElementById("submit-btn").innerText = "Update Entry";
    document.getElementById("transaction-modal").classList.remove("hidden");
}

function deleteExpense(id) {
    if (!confirm("Are you sure you want to delete this transaction?")) return;

    fetch(`/api/expenses/${id}`, { method: "DELETE" })
    .then(res => {
        if (!res.ok && res.status !== 204) throw new Error("Failed to delete transaction");
        showToast("Transaction deleted!", "success");
        loadDashboard();
        loadExpenses();
        loadBudgets();
    })
    .catch(err => showToast(err.message, "error"));
}

function resetForm() {
    document.getElementById("expense-form").reset();
    document.getElementById("expense-id").value = "";
    document.getElementById("modal-title").innerText = "Add New Transaction";
    document.getElementById("submit-btn").innerText = "Save Entry";
    setDefaultDate();
}

// Render Category Chart
function renderCategoryChart(categoryWiseData) {
    const canvas = document.getElementById("categoryChart");
    const fallback = document.getElementById("chart-fallback");

    if (typeof Chart === "undefined") {
        if (canvas) canvas.style.display = "none";
        if (fallback) fallback.classList.remove("hidden");
        return;
    }

    const labels = Object.keys(categoryWiseData);
    const values = Object.values(categoryWiseData);

    if (labels.length === 0) {
        if (categoryChart) {
            categoryChart.destroy();
            categoryChart = null;
        }
        if (canvas) canvas.style.display = "none";
        if (fallback) fallback.classList.remove("hidden");
        return;
    }

    if (canvas) canvas.style.display = "block";
    if (fallback) fallback.classList.add("hidden");

    if (categoryChart) categoryChart.destroy();

    const ctx = canvas.getContext("2d");
    categoryChart = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: [
                    "#2563eb", "#ef4444", "#10b981", "#f59e0b",
                    "#8b5cf6", "#ec4899", "#14b8a6", "#64748b"
                ],
                borderWidth: 2,
                borderColor: "#ffffff"
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: "bottom", labels: { boxWidth: 12 } }
            }
        }
    });
}

// Render Daily Spending Trend Bar Chart
function renderTrendChart(dailySpendingData) {
    const canvas = document.getElementById("trendChart");
    const fallback = document.getElementById("trend-fallback");

    if (typeof Chart === "undefined") {
        if (canvas) canvas.style.display = "none";
        if (fallback) fallback.classList.remove("hidden");
        return;
    }

    const labels = Object.keys(dailySpendingData);
    const values = Object.values(dailySpendingData);

    if (labels.length === 0) {
        if (trendChart) {
            trendChart.destroy();
            trendChart = null;
        }
        if (canvas) canvas.style.display = "none";
        if (fallback) fallback.classList.remove("hidden");
        return;
    }

    if (canvas) canvas.style.display = "block";
    if (fallback) fallback.classList.add("hidden");

    if (trendChart) trendChart.destroy();

    const ctx = canvas.getContext("2d");
    trendChart = new Chart(ctx, {
        type: "bar",
        data: {
            labels: labels,
            datasets: [{
                label: "Daily Spend (₹)",
                data: values,
                backgroundColor: "rgba(37, 99, 235, 0.85)",
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, grid: { color: "#f1f5f9" } },
                x: { grid: { display: false } }
            }
        }
    });
}

function formatCurrency(val) {
    if (val === null || val === undefined) return "0.00";
    return parseFloat(val).toFixed(2);
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/[&<>"']/g, match => {
        const escapeMap = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
        return escapeMap[match];
    });
}