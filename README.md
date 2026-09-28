# FraudWatch — Basic Transaction Anomaly Flagging System

FraudWatch is an enterprise-grade digital transaction monitoring and compliance clearance platform. Built for financial anomaly detection and cybersecurity surveillance, it intercepts inbound transactions, evaluates them against active configurable fraud detection rules (`HIGH_AMOUNT` and `VELOCITY`), isolates anomalies into an analyst triage queue, and enables compliance reviewers to approve or block suspicious activities.

---

## 🏗️ Target Architecture

```
              FraudWatch Unified Platform
      Spring Boot Server (http://localhost:8080)
      +-------------------------------------------+
      |  Frontend (Static HTML5/CSS3/Vanilla JS)  |
      |  http://localhost:8080/                   |
      +-------------------------------------------+
      |  Swagger OpenAPI Documentation            |
      |  http://localhost:8080/swagger-ui/index   |
      +-------------------------------------------+
      |  REST APIs (/api/transactions, /rules, ..)|
      +---------------------+---------------------+
                            |
                            | Spring Data JPA / Hibernate
                            v
                     MySQL Database
                 (localhost:3306/fraudwatch)
```

- **Frontend**: Pure HTML5, modern responsive CSS3, and Vanilla JavaScript (ES6+). Zero frameworks, zero build tools, zero dependencies. Served directly by Spring Boot at `http://localhost:8080/`.
- **Backend**: Java 21, Spring Boot 4.1.1, Spring Data JPA, Hibernate, Jakarta Validation, Maven.
- **Database**: MySQL 8.0 with automated table schema generation and state persistence (`spring.jpa.hibernate.ddl-auto=update`).

---

## 🌟 Core System Capabilities

1. **Transaction Ingestion & Real-time Evaluation**:
   - Records transactions (`POST /api/transactions`) with `sender`, `receiver`, and `amount`.
   - Initial status begins as `COMPLETED`.
   - Backend fraud engine immediately evaluates all active rules across the transaction stream.

2. **Configurable Fraud Detection Engine**:
   - **`HIGH_AMOUNT` Rule**: Evaluates whether transaction `amount > amountThreshold` using exact `BigDecimal` comparison.
   - **`VELOCITY` Rule**: Evaluates frequency burst by counting transactions from the same `sender` within a rolling window (`timestamp - timeWindowMinutes` to `timestamp`). If `count >= transactionCount`, rule triggers.
   - **Multi-Rule Evaluation**: Evaluates **all active rules concurrently** without early return, capturing a full snapshot of all triggered rules.

3. **Anomaly Isolation & Review Queue**:
   - If any active rule is triggered, the transaction status transitions to `FLAGGED`.
   - A `FlaggedTransaction` entity is recorded in MySQL with `reviewStatus = PENDING` and linked to all triggered rules.

4. **Compliance Clearance Desk**:
   - Dedicated review workflow for compliance officers:
     - **Approve**: Sets review status to `APPROVED`, reverts transaction status to `COMPLETED`, persists audit record (`ReviewOutcome`).
     - **Block**: Sets review status to `BLOCKED`, locks transaction status permanently as `BLOCKED`, persists audit record.
   - **Idempotency & Concurrency Protection**: Transactions already reviewed cannot be re-reviewed.

5. **Live Surveillance Dashboard**:
   - Real-time KPI statistics: Total Processed, Completed Normal, Flagged Anomalies, Blocked Threats, Pending Clearance, and Approved.
   - Pure CSS-driven interactive horizontal progress charts for Rule Violations and Transaction Health distributions.
   - Recent transaction audit feed and quick pending review queue.

6. **Dynamic Rule Administration**:
   - Create, edit, and soft-delete (`active = false`) detection rules in real-time without restarting the backend.

---

## 📁 Project Structure

```
FraudWatch/fraudwatch/
├── pom.xml                                   # Maven dependencies & build configuration
├── mvnw / mvnw.cmd                           # Maven wrappers (JDK 21/26 configured)
├── README.md                                 # Complete documentation & test guide
├── src/
│   ├── main/
│   │   ├── java/com/deva/fraudwatch/
│   │   │   ├── FraudwatchApplication.java    # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   └── WebConfig.java            # CORS configuration for http://localhost:5500
│   │   │   ├── controller/
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── RuleController.java
│   │   │   │   ├── ReviewController.java
│   │   │   │   └── DashboardController.java
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
│   │       └── application.properties        # MySQL and Hibernate properties
│   └── test/java/com/deva/fraudwatch/        # Automated test suite (16 passing tests)
│
└── frontend/                                 # Pure HTML5 / CSS3 / Vanilla JS
    ├── index.html                            # Landing page redirect to dashboard
    ├── pages/
    │   ├── dashboard.html                    # Threat telemetry & KPI metrics
    │   ├── transactions.html                 # Transaction registry & submission modal
    │   ├── flagged.html                      # Anomaly incident audit log
    │   ├── reviews.html                      # Compliance clearance desk
    │   └── rules.html                        # Rule configuration & policy tuning
    ├── css/
    │   ├── style.css                         # Dark theme design system, modals, toasts
    │   ├── dashboard.css                     # KPI cards & CSS horizontal progress charts
    │   ├── tables.css                        # Tables, filter bars, account tags, empty states
    │   └── forms.css                         # Inputs, toggles, review card layouts
    └── js/
        ├── config.js                         # Central API_BASE_URL (http://localhost:8080)
        ├── api.js                            # Central Fetch API service layer
        ├── utils.js                          # Toasts, currency, date formatters, modal helpers
        ├── app.js                            # Navigation shell & backend health monitor
        ├── dashboard.js                      # Dashboard charts & KPI synchronization
        ├── transactions.js                   # Transaction ledger & simulation modal
        ├── flagged.js                        # Anomaly filtering & audit log
        ├── reviews.js                        # Clearance desk, approve & block workflows
        └── rules.js                          # Rule creation, dynamic fields & soft-delete
```

---

## 🚀 How to Run the Project

### Prerequisites
- **Java**: JDK 21 or higher installed
- **MySQL**: Running on `localhost:3306` with database `fraudwatch`
- **Python** (or VS Code Live Server): To serve the static frontend

---

### Step 1: Start the Spring Boot Backend

Open a PowerShell terminal in the project root:

```powershell
# Navigate to workspace root
cd c:\Users\devar\OneDrive\Documents\FraudWatch\fraudwatch

# Compile backend and verify clean build
.\mvnw.cmd clean compile

# Run tests to verify all 16 test cases pass
.\mvnw.cmd test

# Start the Spring Boot application (Port 8080)
.\mvnw.cmd spring-boot:run
```

The backend starts at `http://localhost:8080`.

---

### Step 2: Serve the Frontend

Open a second PowerShell terminal:

```powershell
# Navigate to the frontend directory
cd c:\Users\devar\OneDrive\Documents\FraudWatch\fraudwatch\frontend

# Serve using Python's built-in static server (Port 5500)
python -m http.server 5500
```

*(Alternatively, right-click `frontend/index.html` in VS Code and select **"Open with Live Server"**).*

Open your browser and navigate to:
👉 **`http://localhost:5500`** (or `http://127.0.0.1:5500`)

---

## 📡 Backend API Reference

### Transactions API
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/transactions` | Ingests transaction and evaluates fraud rules |
| `GET` | `/api/transactions` | Lists all historical transactions |
| `GET` | `/api/transactions/{id}` | Retrieves transaction by ID |

### Detection Rules API
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/rules` | Creates a new detection rule |
| `GET` | `/api/rules` | Lists all detection rules |
| `GET` | `/api/rules/{id}` | Retrieves rule by ID |
| `PUT` | `/api/rules/{id}` | Updates rule configuration |
| `DELETE` | `/api/rules/{id}` | Soft-deactivates rule (`active = false`) |

### Compliance Reviews API
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/reviews` | Lists all flagged transactions (optional `?status=`) |
| `GET` | `/api/reviews/pending` | Lists transactions awaiting review |
| `GET` | `/api/reviews/{id}` | Retrieves single flagged transaction and audit outcome |
| `POST` | `/api/reviews/{id}/approve` | Approves flagged transaction &rarr; `COMPLETED` |
| `POST` | `/api/reviews/{id}/block` | Blocks flagged transaction &rarr; `BLOCKED` |

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
On `http://localhost:5500/pages/reviews.html`, click **"Approve"** on a flagged item:
- Transaction status reverts to `COMPLETED`.
- Review status becomes `APPROVED`.
- Dashboard "Completed Normal" counter increments.

### Test 5: Clearance Review Block
On `http://localhost:5500/pages/reviews.html`, click **"Block"** on a flagged item:
- Transaction status is permanently locked as `BLOCKED`.
- Review status becomes `BLOCKED`.
- Dashboard "Threats Blocked" counter increments.

---

## 🎓 College Viva / Project Presentation Points

- **Zero-Dependency Static Frontend**: The frontend is built entirely with semantic HTML5, custom dark CSS3, and modern Vanilla JS (ES6+ `fetch`), completely decoupled from the Spring Boot backend.
- **Enterprise Multi-Rule Screening**: Rather than terminating upon the first matched rule, the backend executes an exhaustive evaluation loop over all active rules, attaching every violation to the flagged record.
- **Audit Immutability**: All decisions recorded at the clearance desk are permanently preserved in the `review_outcome` table with reviewer credentials, timestamps, and justification comments.
- **Resilient Error Handling**: Centralized exception handling (`@RestControllerAdvice`) maps validation and business errors to clean JSON responses with RFC 7807 consistency.
