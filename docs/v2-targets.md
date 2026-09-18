# v2 Refactoring Goals

## Technical Stack

| Component          | v1 (Current)                     | v2 (Target)                                                                                     |
| ------------------ | -------------------------------- | ----------------------------------------------------------------------------------------------- |
| Spring Boot        | 2.7.18                           | 3.x                                                                                             |
| Java               | 11                               | 21                                                                                              |
| Database Access    | `JdbcTemplate` in controllers    | JPA/Hibernate, repository pattern                                                               |
| Frontend           | Thymeleaf + Bootstrap 3 + jQuery | React 18 wizard                                                                                 |
| Auth               | BankID mock (hardcoded)          | BankID mock moved to its own service, replaceable with a real integration later                 |
| Passwords          | MD5                              | BCrypt via Spring Security                                                                      |
| Audit Log          | JSON blob in a `TEXT` column     | Separate `audit_events` table with indexes and hash chain for tamper detection                  |
| Company Validation | Mocked without error handling    | Dedicated service with a clear client interface, mocked in MVP, replaceable with real API calls |
| PII                | Plain text                       | Encrypted on the hot path (AES-256 or equivalent, C/C++ via JNA, see `native/README.md`)        |
| Transactions       | None                             | `@Transactional` at the service layer                                                           |
| Session Check      | Copy-pasted into every method    | Spring Security filter chain                                                                    |

## v2 Architecture Goals

```text id="q6l3hf"
Browser (React 18)
  └── REST API → Spring Boot 3 (Java 21)
                  ├── SecurityFilterChain (session/JWT)
                  ├── Controller (thin, no business logic)
                  ├── Service (@Transactional, business logic)
                  │    ├── ScoringService (configurable thresholds)
                  │    ├── AuditService (writes to audit_events table)
                  │    ├── CompanyValidationService (client interface, mocked in MVP)
                  │    ├── BankIdService (mock client, replaceable)
                  │    └── NotificationService (email via Spring Mail)
                  ├── Repository (JPA, Spring Data)
                  └── JNA Bridge → native/libresurs.so (PII encryption, audit signing)
```

## Specific Refactoring Tasks

### 1. Extract ScoringService

* Move the scoring logic from `ApplicationController` (800+ lines) into a separate `ScoringService`
* Define `ScoringThresholds` as a configurable class (`application.properties`)
* Unit test all thresholds

### 2. Separate `audit_events` Table

```sql
CREATE TABLE audit_events (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT REFERENCES applications(id),
    ts TIMESTAMP NOT NULL DEFAULT NOW(),
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(100),
    details JSONB,
    INDEX idx_audit_application_id (application_id),
    INDEX idx_audit_action (action)
);
```

### 3. Spring Security

* Replace copy-pasted session checks with `SecurityFilterChain`
* Replace MD5 with `BCryptPasswordEncoder`
* Implement role-based access control using `@PreAuthorize`

### 4. JNA Integration (`native/`)

* Build a C/C++ module for encrypting sensitive information (organization numbers, personal information, financial information) using AES-256 or an equivalent algorithm, with separate key storage on the hot path
* Build a C/C++ module for secure audit signing: hash chains that detect tampering with the audit log
* Expose both through the JNA bridge (`libresurs.so`)

### 5. BankID Mock as a Separate Service

* Move the BankID mock out of `AuthController` into its own service with a clear client interface
* Keep the mock (happy path) in v2 and make it replaceable with a real BankID integration later
* Validate the authorized signatory's legal signing authority in the mock flow

### 6. `@Transactional`

* Wrap application creation (company + application + audit) in a transaction
* Add optimistic locking to the `Application` entity

### 7. Email Notifications

* Implement Spring Mail
* Send a confirmation when an application is submitted and a notification when a decision is made

### 8. External Company Validation as a Separate Service

* Move company validation out of the controller layer into its own service with a clear client interface
* Mock it in the MVP (registration status, VAT status, and F-tax status), while keeping it replaceable with real API calls later
