# Smart Banking System
## Deploy to Render
This project includes Dockerfile and render.yaml for easy deployment.

Steps:
1. Push this repo to GitHub (done).
2. Create a database (Render Managed MySQL or Railway MySQL) and note credentials.
3. On https://render.com → New → Web Service → Connect this GitHub repo.
   - Environment: Docker
   - Health check path: `/login`
4. Set environment variables on the service:
   - `SPRING_DATASOURCE_URL=jdbc:mysql://<HOST>:<PORT>/<DB>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   - `SPRING_DATASOURCE_USERNAME=<USER>`
   - `SPRING_DATASOURCE_PASSWORD=<PASS>`
   - (Optional) `PORT=8080` (Render sets this automatically)
5. Click Create Web Service and wait for the first deploy to finish.

Notes:
- Application reads `server.port=${PORT:8080}` and runs on the port provided by Render.
- If you see DB errors, verify firewall/allowlist and credentials in env vars.
- For local Docker test:
  - `docker build -t smart-banking .`
  - `docker run -p 8080:8080 -e SPRING_DATASOURCE_URL=... -e SPRING_DATASOURCE_USERNAME=... -e SPRING_DATASOURCE_PASSWORD=... smart-banking`


Tech: Spring Boot, Spring Security, Spring Data JPA, Thymeleaf, MySQL, Maven

## Run locally
1. Ensure Java 17+ and MySQL running.
2. Create DB user and set `spring.datasource.*` in `src/main/resources/application.properties`.
3. Build and run:
```
mvn spring-boot:run
```
4. Visit http://localhost:8080/register to create a user.

## Admin user
- Register a normal user first, then in DB set `role` to `ADMIN` if needed.
 
---

## Overview
Smart Banking System is a full-stack Spring Boot application that provides core retail banking features for end users:

- Secure authentication (BCrypt), session management, role `USER` (admin panel intentionally disabled)
- User dashboard with account number, real-time balance, quick actions, and recent transactions
- Money operations with validations and audit trail
  - Deposit
  - Withdraw (insufficient funds validation)
  - Transfer (by receiver account number; double-entry transactions recorded)
- Full transaction history page
- PDF account statements (date range or last 30 days) using OpenPDF
- Transaction analytics using Chart.js (last 30 days)
  - Amounts per day: Credits vs Debits (line)
  - Counts per day (bar)
  - Totals (pie)
- Responsive, modern UI (Bootstrap + custom CSS)

## Tech Stack
- Backend: Spring Boot, Spring MVC, Spring Security, Spring Data JPA
- View: Thymeleaf
- DB: MySQL
- Build: Maven
- Frontend: Bootstrap 5, Chart.js
- PDF: OpenPDF (iText-compatible)

## Architecture
- `model`: JPA entities `User`, `Account`, `Transaction`
- `repository`: `UserRepository`, `AccountRepository`, `TransactionRepository`
- `service`: `AuthService`, `AccountService`, `StatementService`, `CustomUserDetailsService`
- `controller`: `AuthController`, `DashboardController`, `TransactionController`, `TransactionsController`, `ReportController`
- `security`: Spring Security configuration (form login, logout, role-based access)

## Database Schema
- `users(id, username, password, role, enabled, email)`
- `accounts(id, user_id(FK), account_number, balance)`
- `transactions(id, sender_account, receiver_account, type(DEPOSIT|WITHDRAW|TRANSFER), amount, timestamp)`

Indexes are created implicitly by JPA on primary keys and unique constraints (e.g., username, account_number).

## Configuration
Set the following in `src/main/resources/application.properties`:

```
spring.datasource.url=jdbc:mysql://localhost:3306/smart_banking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.thymeleaf.cache=false
```

Optionally set a different port:

```
server.port=8080
```

## Build & Run
```
mvn clean package
mvn spring-boot:run
```
Navigate to:
- Register: http://localhost:8080/register
- Login: http://localhost:8080/login
- Dashboard: http://localhost:8080/dashboard
- Transactions: http://localhost:8080/transactions

## Features in Detail
### Authentication & Authorization
- BCrypt password hashing
- Role-based: `USER` (admin panel intentionally removed/disabled for this build)
- CSRF disabled for simplicity of demo; enable it for production

### Money Operations
- Deposit: increases balance and logs transaction
- Withdraw: validates sufficient funds; logs transaction
- Transfer: between two account numbers; logs both sides appropriately

### Transactions & Statements
- Full history table with type, from, to, amount, timestamp
- Download PDF statement:
  - Transactions page: date-range form + Download button
  - Dashboard: Statement (PDF) quick action
  - Direct endpoint: `/statement.pdf?from=YYYY-MM-DD&to=YYYY-MM-DD` (defaults to last 30 days if omitted)

### Analytics (Last 30 Days)
- Endpoint: `/analytics/data` returns JSON with:
  - `labels` (dates)
  - `credits`, `debits` (amounts/day)
  - `creditsCount`, `debitsCount` (counts/day)
  - `totals` = `{ totalCredits, totalDebits, net }`
  - `typeCounts` = `{ deposit, withdraw, transferIn, transferOut }`
- Dashboard charts:
  - Line: Credits vs Debits
  - Bar: Counts per day
  - Pie: Totals
- Transactions page chart:
  - Line: Credits vs Debits

## API Endpoints (User-facing pages)
- `GET /register`, `POST /register`
- `GET /login`, `POST /login`, `GET /logout`
- `GET /dashboard`
- `POST /deposit`, `POST /withdraw`, `POST /transfer`
- `GET /transactions`
- `GET /statement.pdf` (query params: `from`, `to` optional)
- `GET /analytics/data` (JSON for charts)

## Project Structure
```
smart-banking-system/
├── src/main/java/com/bankapp/
│   ├── SmartBankingApplication.java
│   ├── controller/
│   ├── model/
│   ├── repository/
│   ├── security/
│   └── service/
├── src/main/resources/
│   ├── application.properties
│   ├── templates/ (Thymeleaf HTML)
│   └── static/
│       ├── css/style.css
│       └── js/
└── pom.xml
```

## Troubleshooting
- Port already in use (8080): stop previous instance or change `server.port`
- 404 on `/analytics/data` or blank charts: restart the server and hard refresh browser (Ctrl+F5)
- DB connection errors: verify MySQL is running and credentials/URL are correct

## Notes
- This build purposefully removes/disables the admin panel per requirements
- Sample admin creation: set `role='ADMIN'` manually in DB after registration (if needed later)

## License
This project is provided as-is for demonstration and educational purposes. Review third-party licenses (OpenPDF, Bootstrap, Chart.js) before production use.
