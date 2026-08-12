# 💼 Smart Expense Tracker

An internship-ready, clean, modern Java Spring Boot application for personal finance management, expense tracking, category budgeting, and batch CSV imports. Built with clean layered architecture, dynamic JPA Specifications, atomic batch processing, Actuator observability, GitHub Actions CI, precision `BigDecimal` math, and a modern SaaS dashboard UI.

---

## 🚀 Key Features

- **Transaction Management (CRUD):** Add, view, edit, and delete income and expense records with category tags, payment methods, and notes.
- **Advanced Multi-Condition Search (JPA Specifications):** Dynamic database filtering by date range, min/max amount, category, payment method, transaction type, and keyword search without native SQL.
- **Category Budget Management:** Define monthly category budgets (`YYYY-MM`), with real-time percentage progress meters and status badges (`UNDER_BUDGET` <80%, `NEAR_LIMIT` 80–99%, `EXCEEDED` ≥100%).
- **Atomic CSV Expense Import:** Upload bank statement CSV files with full pre-validation. If any row fails validation, 0 rows are persisted (atomic rollback) and a line-by-row error report is generated.
- **Financial Analytics & Insights:** Real-time metrics for Total Expenses, Total Income, Net Balance, Category Doughnut Breakdown, Daily Spending Trend, and Rule-Based Insights.
- **Spring Boot Actuator Observability:** Health and info monitoring exposed at `/actuator/health` and `/actuator/info`.
- **Monetary Precision:** Uses `BigDecimal` throughout service calculations and persistence models to prevent floating-point rounding errors.
- **Dual Database Compatibility:** Runs out-of-the-box with **H2** (in-memory default) for zero-setup execution and automated testing, with environment-variable support for **MySQL**.
- **GitHub Actions CI:** Automated pipeline running `./mvnw clean test` on Java 21 for every push and pull request.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Language & JDK** | Java 21 |
| **Framework** | Spring Boot 3.4.2 |
| **Web & API** | Spring MVC, REST APIs, Thymeleaf |
| **Persistence** | Spring Data JPA, Hibernate, JPA Specifications |
| **Observability** | Spring Boot Actuator (`/actuator/health`) |
| **Database** | H2 (Dev/Testing), MySQL (Production via Env Vars) |
| **Validation** | Jakarta Bean Validation |
| **CI/CD** | GitHub Actions (`.github/workflows/maven.yml`) |
| **Testing** | JUnit 5, Mockito, Spring MockMvc, DataJpaTest (59 Tests) |
| **Frontend** | HTML5, Vanilla CSS3 (SaaS Design System), JavaScript (ES6+), Chart.js |
| **Build Tool** | Apache Maven (with Maven Wrapper) |

---

## 🏗️ Architecture & Package Structure

The application follows a standard **Layered Architecture** with strict separation of concerns:

```
Controller (HTTP Request/Response & Mapping)
    ↓
Service (Business Logic, Transactions, BigDecimal Math & Validation)
    ↓
Repository (JPA Specifications & Database-Portable Queries)
    ↓
Database (H2 / MySQL)
```

```
com.sathwik.expensetracker
├── ExpenseTrackerApplication.java
├── controller/
│   ├── ExpenseController.java        # Core REST endpoints & Specification search
│   ├── BudgetController.java         # Category budget CRUD & status APIs
│   ├── ExpenseImportController.java  # CSV batch import API
│   └── ViewController.java           # Dashboard UI page routing
├── service/
│   ├── ExpenseService.java           # Core expense business logic & dashboard compiler
│   ├── BudgetService.java            # Budget calculation & status logic
│   └── CsvImportService.java         # CSV validation & atomic batch processing
├── repository/
│   ├── ExpenseRepository.java        # JPA Repository & JpaSpecificationExecutor
│   ├── BudgetRepository.java         # Category budget persistence
│   ├── UserRepository.java           # User persistence
│   └── specification/
│       └── ExpenseSpecification.java # Dynamic Criteria Builder specifications
├── entity/
│   ├── Expense.java                  # JPA Entity for expenses
│   ├── Budget.java                   # JPA Entity for category budgets (Unique constraint)
│   └── User.java                     # JPA Entity for user metadata
├── dto/
│   ├── ExpenseRequest.java           # Expense creation DTO
│   ├── ExpenseSearchRequest.java     # Advanced search filter DTO
│   ├── ExpenseResponse.java          # Expense response DTO
│   ├── BudgetRequest.java           # Budget creation DTO
│   ├── BudgetResponse.java          # Budget response DTO
│   ├── BudgetStatusResponse.java    # Budget progress metric DTO
│   ├── DashboardResponse.java        # Aggregated dashboard metrics DTO
│   ├── ImportRowError.java           # Row validation error DTO
│   └── ImportSummaryResponse.java    # CSV import summary DTO
├── enums/
│   ├── ExpenseType.java              # EXPENSE, INCOME
│   ├── PaymentMethod.java            # CASH, UPI, CREDIT_CARD, DEBIT_CARD, NET_BANKING, OTHER
│   └── BudgetStatus.java             # UNDER_BUDGET, NEAR_LIMIT, EXCEEDED
└── exception/
    ├── GlobalExceptionHandler.java    # @RestControllerAdvice for uniform errors
    ├── ResourceNotFoundException.java
    └── ErrorResponse.java            # Standard JSON error payload
```

---

## 📡 REST API Specification

### 1. Expense APIs
| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/expenses` | Create a new transaction | `201 Created` |
| `GET` | `/api/expenses/user/{userId}` | Get all transactions for user | `200 OK` |
| `GET` | `/api/expenses/user/{userId}/search` | Advanced JPA Specification search | `200 OK` |
| `GET` | `/api/expenses/{id}` | Get transaction details by ID | `200 OK` |
| `PUT` | `/api/expenses/{id}` | Update an existing transaction | `200 OK` |
| `DELETE` | `/api/expenses/{id}` | Delete a transaction by ID | `204 No Content` |
| `GET` | `/api/expenses/dashboard/{userId}` | Get aggregated dashboard metrics | `200 OK` |
| `POST` | `/api/expenses/import` | Import transactions via CSV upload | `200 OK` / `400 Bad Request` |

### 2. Category Budget APIs
| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/budgets` | Create category monthly budget | `201 Created` |
| `GET` | `/api/budgets/user/{userId}` | List all budgets for user | `200 OK` |
| `PUT` | `/api/budgets/{id}` | Update monthly budget limit | `200 OK` |
| `DELETE` | `/api/budgets/{id}` | Delete a budget configuration | `204 No Content` |
| `GET` | `/api/budgets/user/{userId}/status` | Get budget progress & status metrics | `200 OK` |

### 3. Observability APIs
| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/actuator/health` | Application & database health check | `200 OK` (`"status": "UP"`) |
| `GET` | `/actuator/info` | Application build info | `200 OK` |

---

## 📄 CSV Import Format

Upload a `.csv` file with the exact header format:

```csv
date,description,category,amount,paymentMethod,type
2026-08-01,Restaurant,Food,450.00,UPI,EXPENSE
2026-08-03,Metro Pass,Transport,80.00,UPI,EXPENSE
2026-08-04,Monthly Salary,Salary,50000.00,NET_BANKING,INCOME
```

---

## ⚡ Setup & Execution Instructions

### Prerequisites
- JDK 21 installed.
- Git.

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/smart-expense-tracker.git
cd smart-expense-tracker
```

### 2. Run Automated Test Suite (59 Tests)
```bash
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

### 3. Build Production Package
```bash
# Windows
.\mvnw.cmd clean package

# Linux / macOS
./mvnw clean package
```

### 4. Run the Application (Zero Setup - H2 Database)
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Access the Dashboard UI in your browser at:
**`http://localhost:8080/dashboard-ui`** (or **`http://localhost:8080/`**)

Verify Actuator Health check at:
**`http://localhost:8080/actuator/health`**

---

## 🗄️ Database Configuration

### H2 Database (Default)
Runs using an in-memory H2 database requiring zero installation (`jdbc:h2:mem:expensedb`).

### MySQL Database (Optional Environment Override)
To connect to MySQL, export the following environment variables:

```bash
export DB_URL="jdbc:mysql://localhost:3306/expense_db?useSSL=false&serverTimezone=UTC"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
export DB_DRIVER="com.mysql.cj.jdbc.Driver"
```

---

## 🧪 Automated Test Suite (59 Test Cases)

- **`ExpenseSpecificationTest` (8 tests):** Validates dynamic multi-condition database queries against real records.
- **`BudgetServiceTest` & `BudgetControllerTest` (16 tests):** Validates category budget creation, uniqueness, updates, status rules (`UNDER_BUDGET`, `NEAR_LIMIT`, `EXCEEDED`), and `BigDecimal` limits.
- **`CsvImportServiceTest` & `ExpenseImportControllerTest` (10 tests):** Validates CSV header parsing, date/amount validations, and atomic rollback (0 rows saved on error).
- **`ActuatorHealthTest` (1 test):** Verifies `/actuator/health` returns status `UP`.
- **`ExpenseServiceTest`, `ExpenseControllerTest`, `ExpenseRepositoryTest` (24 tests):** Core CRUD and analytics verification.

---

## 📌 Honest Limitations

- **Single-User Scope:** Currently operates on a single-user reference model (`userId = 1`) without authentication. Spring Security and JWT can be integrated in future phases.

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for details.
