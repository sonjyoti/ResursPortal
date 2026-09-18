# Architecture – Resurs Credit Application v1

## Overview

Spring Boot 2.7 monolith. All logic is contained in the controllers. There is no service layer and no repository layer.

## Stack

* **Backend:** Spring Boot 2.7.18, Java 11
* **Database:** PostgreSQL 12
* **Template:** Thymeleaf + Bootstrap 3 (CDN)
* **Frontend:** jQuery multi-step form (`steps.js`)
* **ORM:** `JdbcTemplate` used directly in controllers
* **Authentication:** BankID mock (hardcoded `if` statement) + MD5 passwords

## Component Diagram (Text Form)

```text
Browser
  └── HTTP → Spring Boot (port 8083)
               ├── AuthController          → JdbcTemplate → PostgreSQL
               ├── ApplicationController   → JdbcTemplate → PostgreSQL
               ├── DocumentController      → /tmp/uploads/ + JdbcTemplate → PostgreSQL
               ├── StatusController        → JdbcTemplate → PostgreSQL
               └── BackofficeController    → JdbcTemplate → PostgreSQL
```

## Known Architectural Problems (Educational)

1. **No service layer** — all business logic is directly inside the controllers
2. **Audit log stored as a JSON blob** — `audit_log TEXT` on the `applications` row, with no separate table or index
3. **Files stored in `/tmp`** — files are deleted on restart and are not persistent
4. **BankID mock** — organization numbers are hardcoded in an `if` statement
5. **MD5 passwords** — weak hashing with no salt
6. **PII stored in plain text** — organization number, company name, and authorized signatory are not encrypted
7. **SQL injection** — case worker login builds SQL using string concatenation
8. **No transaction** — three separate `INSERT` statements are executed without `BEGIN/COMMIT`
9. **Magic numbers** — the equity ratio threshold is `0.25`, `0.20`, **and** `0.30` in different places
10. **Copy-pasted session checks** — `if (session.getAttribute("userId") == null)` is repeated in every method
