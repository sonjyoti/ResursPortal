# Known Bugs and Security Issues – v1

## Security Issues

### 1. SQL Injection – Case Worker Login

**File:** `AuthController.java`
**Code:** `"SELECT * FROM case_workers WHERE email = '" + email + "' AND password_md5 = '" + md5 + "'"`
**Risk:** Full database control through the email field (e.g. `' OR '1'='1`)

### 2. PII Stored in Plain Text

**File:** `ApplicationController.java`, database
**Problem:** Company name, organization number, and authorized signatory are stored unencrypted in PostgreSQL.
**Comment in code:** `// TODO: encrypt PII before go-live`

### 3. MD5 Passwords

**File:** `AuthController.java`, `infra/seed.sql`
**Problem:** MD5 is cryptographically broken, provides no salt, and is vulnerable to rainbow table attacks.

### 4. BankID Mock Implemented as a Hardcoded `if` Statement

**File:** `AuthController.java`
**Code:** `if (orgNumber.equals("556000-1234") || orgNumber.equals("556000-5678"))`
**Risk:** Anyone who knows a valid organization number can log in.

## Data Integrity Issues

### 5. No Transaction When Creating an Application

**File:** `ApplicationController.java`, POST `/apply`
**Problem:** Three separate `INSERT` statements (company, application, audit log) are executed without `BEGIN/COMMIT`.

If the application crashes halfway through the operation, inconsistent data may be created.

### 6. Audit Log Is Not Searchable or Indexed

**Problem:** `audit_log TEXT` is a JSON blob stored in the applications row.

It is impossible to efficiently search by event type, filter by date, or perform aggregations.

The log is updated using manual string manipulation:

```java
currentLog.substring(0, currentLog.lastIndexOf("]"))
```

## Business Logic Bugs

### 7. Inconsistent Equity Ratio Thresholds

**File:** `ApplicationController.java`

* Around line ~115: `if (soliditet < 0.20)` → hard reject
* Around line ~122: `if (soliditet < 0.25)` → flag
* Around line ~160: `if (soliditet < 0.30 && requestedAmount > 1000000)` → flag (third threshold)
* `soliditetCategory()` method: `if (soliditet < 0.15)` → CRITICAL (fourth threshold, never called)

### 8. PDF Is Not Parsed

**File:** `DocumentController.java`

**Comment:**

```java
// TODO: implement PDF parsing in v2 (see native/README.md)
```

The file is saved to `/tmp/uploads/`, but its contents are never read.

Scoring is based entirely on manually entered financial key figures.

**Note:** PDF parsing is **not part of the v2 scope** (see `docs/v2-targets.md`). Uploaded documents are instead reviewed manually by case officers.

## Operational Issues

### 9. Files Stored in `/tmp`

Uploaded PDF files are stored in `/tmp/uploads/`.

They are deleted when the container restarts.

Users can see the documents in the database but cannot download them after a restart.

### 10. No Pagination

`BackofficeController.java` retrieves **all `UNDER_REVIEW` applications** without a `LIMIT`.

At high volumes, this can lead to memory problems and slow responses.
