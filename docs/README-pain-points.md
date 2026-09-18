# Pain Points – Resurs Credit Application v1

## What Works

* **Basic application flow:** Company logs in (BankID mock) → enters financial key figures → scoring runs → decision is returned
* **Scoring decisions:** `APPROVED` / `UNDER_REVIEW` / `REJECTED` with an explanation
* **Document upload:** Files are uploaded and stored (but not parsed)
* **Case officer interface:** Karin can view `UNDER_REVIEW` applications and make decisions
* **Audit log:** Events are logged, although they are stored as a JSON blob

## What Breaks

### Concurrent Applications

There is no locking.

If two users from the same organization number submit an application simultaneously, the company `INSERT` may be executed twice. The `UNIQUE` constraint then throws an exception and may leave a partially created application record.

### PDF Parsing Is Missing

The system accepts PDF files but never reads them.

Scoring is based on manually entered figures, so an error in the form can result in an incorrect decision. The actual contents of the annual report are never automatically verified.

**Note:** PDF parsing is **not part of the v2 scope** (see `docs/v2-targets.md`). Uploaded documents are instead manually reviewed by case officers in borderline cases.

### Audit Log Is Not Searchable

`audit_log TEXT` is a JSON blob.

It is not possible to efficiently run queries such as:

```sql
SELECT *
FROM applications
WHERE audit_log LIKE '%SCORING_RUN%';
```

This results in a full table scan and makes it difficult to aggregate events by type.

### `/tmp` Is Cleared

Uploaded files disappear when the container restarts.

Documents remain visible in the database, but attempting to download them results in a `404`.

### Magic Numbers in Scoring

The equity ratio threshold appears in three different places with three different values:

```text
0.20
0.25
0.30
```

Changing a business rule therefore requires searching through the entire file.

### Copy-Pasted Session Checks

There are 10+ copies of:

```java
if (session.getAttribute("userId") == null)
    return "redirect:/login";
```

A Spring `HandlerInterceptor` could handle this in a single place.

### No Email Notifications

When a case officer makes a decision, the company receives no notification.

The company must log in to the portal and check the status manually.

### SQL Injection

The case worker login is vulnerable to SQL injection.

The vulnerability is caused by string concatenation instead of using `PreparedStatement` parameters.
