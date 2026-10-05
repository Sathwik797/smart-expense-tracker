# 💼 Smart Expense Tracker

A Spring Boot personal-finance application for expense tracking, category budgets, CSV imports, financial analytics, and dashboard reporting.

## ✨ Highlights

- Transaction CRUD for income and expenses
- Multi-condition search with JPA Specifications
- Monthly category budgets with status tracking
- Atomic CSV imports with row-level validation
- Financial analytics and dashboard metrics
- BigDecimal-based monetary calculations
- H2 for zero-setup development and testing
- Optional MySQL configuration
- Spring Boot Actuator health/info endpoints
- GitHub Actions CI
- Automated test suite

## 🏗️ Architecture

```text
Controller
    ↓
Service
    ↓
Repository / JPA Specifications
    ↓
H2 or MySQL
```

The application uses layered architecture with DTOs, validation, exception handling, transactional business logic, and repository-level querying.

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.2 |
| Persistence | Spring Data JPA / Hibernate |
| Database | H2 / MySQL |
| API | Spring MVC / REST |
| Validation | Jakarta Bean Validation |
| Observability | Spring Boot Actuator |
| Frontend | Thymeleaf, HTML, CSS, JavaScript, Chart.js |
| Testing | JUnit 5, Mockito, MockMvc |
| CI | GitHub Actions |
| Build | Maven |

## 🚀 Run Locally

Clone the repository:

```bash
git clone https://github.com/Sathwik797/smart-expense-tracker.git
cd smart-expense-tracker
```

Run the test suite:

```bash
# Windows
.\\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

Start the application:

```bash
# Windows
.\\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Open:

```text
http://localhost:8080/dashboard-ui
```

The default H2 configuration requires no external database setup.

## 📡 Core APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/expenses` | Create transaction |
| GET | `/api/expenses/user/{userId}` | List transactions |
| GET | `/api/expenses/user/{userId}/search` | Filter transactions |
| GET | `/api/expenses/dashboard/{userId}` | Dashboard analytics |
| POST | `/api/expenses/import` | Import CSV transactions |
| POST | `/api/budgets` | Create monthly budget |
| GET | `/actuator/health` | Health check |

## 🧪 Testing

The repository includes automated tests covering controllers, services, repositories, specifications, budget rules, CSV validation/rollback, and Actuator health.

Run:

```bash
./mvnw clean test
```

## 💰 Monetary Safety

Financial amounts use `BigDecimal` rather than floating-point types to avoid common precision problems in monetary calculations.

## 📥 CSV Import Safety

CSV imports are validated before persistence. Invalid input produces an error response rather than partially persisting a batch.

## 📌 Scope

This is currently a single-user reference application without authentication. Spring Security/JWT can be added in a future iteration.

## 👨‍💻 Author

**Sathwik Reddy**

GitHub: https://github.com/Sathwik797
