# FraudWatch — Basic Transaction Anomaly Flagging System

FraudWatch is an enterprise-grade digital transaction monitoring and compliance clearance platform. Built for financial anomaly detection and cybersecurity surveillance, it intercepts inbound transactions, evaluates them against active configurable fraud detection rules (`HIGH_AMOUNT` and `VELOCITY`), isolates anomalies into an analyst triage queue, and enables compliance reviewers to approve or block suspicious activities.

The entire system runs as a single unified Spring Boot application serving both the REST APIs, OpenAPI/Swagger UI, and the minimal Navy/White frontend from port 8080.

---

## 🏗️ Target Architecture

```
              FraudWatch Unified Platform
       Spring Boot Server (http://localhost:8080)
       +-------------------------------------------+
       |  Frontend (Static HTML5/CSS3/Vanilla JS)  |
       |  http://localhost:8080/                   |
       +-------------------------------------------+
       |  Swagger OpenAPI 3.0 Documentation        |
       |  http://localhost:8080/swagger-ui/index.html
       +-------------------------------------------+
       |  REST APIs (/api/transactions, /rules, ..)|
       +---------------------+---------------------+
                             |
                             | Spring Data JPA / Hibernate
                             v
                      MySQL Database
                  (localhost:3306/fraudwatch)
```

- **Frontend**: Pure HTML5, minimal Navy (`#0F172A`) & White (`#FFFFFF`) cybersecurity design system, and Vanilla JavaScript (ES6+). Zero third-party JS/CSS frameworks, zero build tools, zero dependencies. Served directly by Spring Boot from `src/main/resources/static/` at `http://localhost:8080/`.
- **Backend**: Java 21, Spring Boot 4.1.1, Spring Data JPA, Hibernate, Jakarta Validation, Springdoc OpenAPI 2.8.5, Maven.
- **Database**: MySQL 8.0 with automated table schema generation and state persistence (`spring.jpa.hibernate.ddl-auto=update`).

---

## 🌟 Core System Capabilities

1. **Transaction Ingestion & Real-Time Evaluation**:
   - Records transactions (`POST /api/transactions`) with `sender`, `receiver`, and `amount`.
   - Initial status begins as `COMPLETED`.
   - Backend fraud engine immediately evaluates all active rules across the transaction stream.
   - Supports Full Transaction CRUD: Create, Read, Update (re-evaluates fraud triggers), and Soft-Delete.

2. **Configurable Fraud Detection Engine**:
   - **`HIGH_AMOUNT` Rule**: Evaluates whether transaction `amount > amountThreshold` using exact `BigDecimal` comparison.
   - **`VELOCITY` Rule**: Evaluates frequency burst by counting transactions from the same `sender` within a rolling window (`timestamp - timeWindowMinutes` to `timestamp`). If `count >= transactionCount`, rule triggers.
   - **Multi-Rule Evaluation**: Evaluates **all active rules concurrently** without early return, capturing a full snapshot of all triggered rules.

3. **3-State Rule Lifecycle Management**:
   - Rules support three distinct states:
     - **ACTIVE**: `active = true`, `deleted = false` (participates in fraud checks).
     - **DISABLED**: `active = false`, `deleted = false` (temporarily inactive).
     - **DELETED**: `active = false`, `deleted = true` (soft-deleted, never physically erased from MySQL).
   - Soft-deleted rules are automatically excluded from the rule list and UI.

4. **Anomaly Isolation & Review Queue**:
   - If any active rule is triggered, the transaction status transitions to `FLAGGED`.
   - A `FlaggedTransaction` entity is recorded in MySQL with `reviewStatus = PENDING` and linked to all triggered rules.

5. **Compliance Clearance Desk**:
   - Dedicated review workflow for compliance officers:
     - **Approve**: Sets review status to `APPROVED`, reverts transaction status to `COMPLETED`, persists audit record (`ReviewOutcome`).
     - **Block**: Sets review status to `BLOCKED`, locks transaction status permanently as `BLOCKED`, persists audit record.
   - **Idempotency & Concurrency Protection**: Transactions already reviewed cannot be re-reviewed.

6. **Live Surveillance Dashboard**:
   - Real-time KPI statistics: Total Processed, Completed Normal, Flagged Anomalies, Blocked Threats, Pending Clearance, and Approved.
   - Pure CSS-driven interactive horizontal progress charts for Rule Violations and Transaction Health distributions.
   - Recent transaction audit feed and quick pending review queue.

---

## 📁 Project Structure

```
FraudWatch/fraudwatch/
├── pom.xml                                   # Maven dependencies & Springdoc OpenAPI build config
├── mvnw / mvnw.cmd                           # Maven wrappers
├── README.md                                 # Complete documentation & test guide
├── src/
│   ├── main/
│   │   ├── java/com/deva/fraudwatch/
│   │   │   ├── FraudwatchApplication.java    # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java        # Swagger / OpenAPI 3.0 configuration
│   │   │   │   └── WebConfig.java            # WebMvc resource handlers and CORS config
│   │   │   ├── controller/
│   │   │   │   ├── TransactionController.java # Full CRUD for transactions
│   │   │   │   ├── RuleController.java       # Lifecycle management & soft delete
│   │   │   │   ├── ReviewController.java     # Clearance desk workflow
│   │   │   │   └── DashboardController.java  # Aggregated metrics & telemetry
│   │   │   ├── service/
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── FraudDetectionService.java # Multi-rule anomaly screening
│   │   │   │   ├── RuleService.java
│   │   │   │   ├── ReviewService.java
│   │   │   │   └── DashboardService.java
│   │   │   ├── repository/
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   ├── RuleRepository.java
│   │   │   │   ├── FlaggedTransactionRepository.java
│   │   │   │   └── ReviewOutcomeRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── Rule.java
│   │   │   │   ├── FlaggedTransaction.java
│   │   │   │   └── ReviewOutcome.java
│   │   │   ├── dto/
│   │   │   │   ├── TransactionRequest.java
│   │   │   │   ├── TransactionResponse.java
│   │   │   │   ├── RuleRequest.java
│   │   │   │   ├── RuleResponse.java
│   │   │   │   ├── ReviewRequest.java
│   │   │   │   ├── ReviewResponse.java
│   │   │   │   └── DashboardResponse.java
│   │   │   ├── enums/
│   │   │   │   ├── TransactionStatus.java    # COMPLETED, FLAGGED, BLOCKED
│   │   │   │   ├── RuleType.java             # HIGH_AMOUNT, VELOCITY
│   │   │   │   └── ReviewStatus.java         # PENDING, APPROVED, BLOCKED
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ResourceNotFoundException.java
│   │   │       ├── BusinessRuleException.java
│   │   │       └── ErrorResponse.java
│   │   └── resources/
│   │       ├── application.properties        # MySQL and Hibernate properties
│   │       └── static/                       # Unified Frontend (HTML/CSS/JS)
│   │           ├── index.html                # App shell & SPA routing
│   │           ├── pages/
│   │           │   ├── dashboard.html        # Telemetry & KPI metrics
│   │           │   ├── transactions.html     # Transaction ledger & CRUD modals
│   │           │   ├── flagged.html          # Anomaly incident audit log
│   │           │   ├── reviews.html          # Compliance clearance desk
│   │           │   └── rules.html            # Rule configuration & policy tuning
│   │           ├── css/
│   │           │   ├── style.css             # Navy & White design system, modals, toasts
│   │           │   ├── dashboard.css         # KPI cards & progress metrics
│   │           │   ├── tables.css            # Clean data tables, status badges, action buttons
│   │           │   └── forms.css             # Inputs, modals, toggle switches
│   │           └── js/
│   │               ├── config.js             # API base URL configuration
│   │               ├── api.js                # Central Fetch API client
│   │               ├── utils.js              # Toasts, currency, date formatters, modal helpers
│   │               ├── app.js                # Navigation shell & router
│   │               ├── dashboard.js          # Dashboard telemetry synchronization
│   │               ├── transactions.js       # Transaction CRUD & simulation modal
│   │               ├── flagged.js            # Anomaly filtering & audit log
│   │               ├── reviews.js            # Clearance desk approve/block workflows
│   │               └── rules.js              # Rule creation, editing & soft deletion
│   └── test/java/com/deva/fraudwatch/        # Automated test suite (31 passing tests)
```

---

## 🚀 How to Run the Project

### Prerequisites
- **Java**: JDK 21 installed (`java -version`)
- **MySQL**: Running on `localhost:3306` with database `fraudwatch` (username `root`, password `root`)

---

### Run Application (Unified Server)

Open a PowerShell terminal in the project directory:

```powershell
cd c:\Users\devar\OneDrive\Documents\FraudWatch\fraudwatch

# Compile backend and verify clean build
.\mvnw.cmd clean compile

# Run complete test suite (31 tests)
.\mvnw.cmd test

# Start the unified Spring Boot application (Port 8080)
.\mvnw.cmd spring-boot:run
```

Once started:
- 🌐 **Web Application**: [`http://localhost:8080/`](http://localhost:8080/)
- 📖 **Interactive Swagger UI**: [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)
- 📄 **OpenAPI v3 JSON Specification**: [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs)

---

## 📡 Backend API Reference

### Transactions API
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/transactions` | Ingests transaction and evaluates fraud rules |
| `GET` | `/api/transactions` | Lists all active transactions (excludes soft-deleted) |
| `GET` | `/api/transactions/{id}` | Retrieves transaction by ID |
| `PUT` | `/api/transactions/{id}` | Updates transaction amount/sender/receiver & re-evaluates rules |
| `DELETE` | `/api/transactions/{id}` | Soft-deletes transaction and associated pending flags |

### Detection Rules API
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/rules` | Creates a new detection rule (`active = true, deleted = false`) |
| `GET` | `/api/rules` | Lists all active & disabled rules (`deleted = false`) |
| `GET` | `/api/rules/{id}` | Retrieves rule by ID |
| `PUT` | `/api/rules/{id}` | Updates rule configuration or toggles enable/disable |
| `DELETE` | `/api/rules/{id}` | Soft-deletes rule (`deleted = true, active = false`) |

### Compliance Reviews API
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/reviews` | Lists all flagged transactions (optional `?status=PENDING`) |
| `GET` | `/api/reviews/pending` | Lists transactions awaiting review |
| `GET` | `/api/reviews/{id}` | Retrieves single flagged transaction and audit outcome |
| `POST` | `/api/reviews/{id}/approve` | Approves flagged transaction &rarr; status returns to `COMPLETED` |
| `POST` | `/api/reviews/{id}/block` | Blocks flagged transaction &rarr; status locked as `BLOCKED` |

### Surveillance Dashboard API
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard` | Aggregated transaction and review metrics |
| `GET` | `/api/dashboard/rules` | Breakdown of trigger counts per rule |

---

## 🧪 Comprehensive End-to-End Test Sequence

You can test the complete system either via the **FraudWatch Web UI** or via **PowerShell / cURL**:

### Test 1: Normal Transaction (< Threshold)
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/transactions" `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"sender":"ACC001","receiver":"ACC002","amount":2500}'
```
**Expected Outcome**: Returns HTTP 201 with `status = "COMPLETED"`. No rules triggered.

### Test 2: High Amount Anomaly (> ₹10,000)
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/transactions" `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"sender":"ACC001","receiver":"ACC003","amount":75000}'
```
**Expected Outcome**: Returns HTTP 201 with `status = "FLAGGED"`. Anomaly appears in Clearance Desk (`/pages/reviews.html`).

### Test 3: Velocity Anomaly (Burst Frequency)
Submit 5 rapid transactions from `ACC005`:
```powershell
1..5 | ForEach-Object {
    Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/transactions" `
      -Headers @{ "Content-Type" = "application/json" } `
      -Body '{"sender":"ACC005","receiver":"ACC006","amount":100}'
}
```
**Expected Outcome**: Transaction 5 triggers the `VELOCITY` rule and is marked `FLAGGED`.

### Test 4: Clearance Review Approval
On `http://localhost:8080/` (Reviews section), click **"Approve"** on a flagged item:
- Transaction status reverts to `COMPLETED`.
- Review status becomes `APPROVED`.
- Dashboard "Completed Transactions" counter increments.

### Test 5: Clearance Review Block
On `http://localhost:8080/` (Reviews section), click **"Block"** on a flagged item:
- Transaction status is permanently locked as `BLOCKED`.
- Review status becomes `BLOCKED`.
- Dashboard "Blocked Transactions" counter increments.

---

## 🎓 Key Engineering Highlights

- **Unified Single-Server Deployment**: The frontend is embedded directly within Spring Boot's static resources, running from a single port (`8080`) with zero CORS complications.
- **Enterprise Multi-Rule Screening**: Rather than terminating upon the first matched rule, the backend executes an exhaustive evaluation loop over all active rules, attaching every violation to the flagged record.
- **Soft Deletion & Audit Integrity**: Deleting rules or transactions marks them as soft-deleted, preventing database orphans while preserving audit integrity for past compliance reviews.
- **Strict Two-Color Design System**: Clean, minimal cybersecurity visual aesthetic using Navy (`#0F172A`) and White (`#FFFFFF`) with zero unnecessary CSS framework bloat.
