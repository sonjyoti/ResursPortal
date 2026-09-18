# Resurs Kreditansökan Portal

B2B credit application portal for Resurs Bank. Companies apply for credit, upload annual reports, and receive a credit decision based on financial key figures.

## Quick Start

```bash
cd infra && docker compose up
```

Open http://localhost:8083

### Test Logins

| Role                           | Credentials                       |
| ------------------------------ | --------------------------------- |
| Company (Malmö Fastigheter AB) | Org. no.: `556000-1234`           |
| Company (Göteborg Handel AB)   | Org. no.: `556000-5678`           |
| Case Officer                   | `karin@resurs.se` / `password123` |

## Folder Structure

```text
backend/ResursPortal/   ← Spring Boot 2.7 Maven project
  src/main/java/se/comerit/resurs/
    ResursPortalApplication.java
    controller/
      AuthController.java        ← BankID mock + MD5 login
      ApplicationController.java ← 800+ lines of scoring logic inline
      DocumentController.java    ← file upload (PDF is not parsed)
      StatusController.java      ← status + hardcoded ETAs
      BackofficeController.java  ← case officer interface
  src/main/resources/
    application.properties
    templates/                   ← Thymeleaf + Bootstrap 3
    static/steps.js              ← jQuery multi-step logic

infra/
  docker-compose.yml             ← PostgreSQL + Spring Boot
  seed.sql                       ← schema + seed data

native/
  README.md                      ← v2 C/C++ modules (PII encryption, audit signing)

docs/
  architecture.md
  known-bugs.md
  README-pain-points.md
  v2-targets.md
```

## Intentional Anti-Patterns (Educational)

This is a **v1 spaghetti codebase** intended for students to refactor into v2.

See `docs/known-bugs.md` for the complete list. Highlights:

1. BankID mock implemented as a hardcoded `if` statement
2. 800+ lines of scoring logic inline in the controller
3. SQL injection in case officer login
4. Audit log stored as a JSON blob (no separate table)
5. `JdbcTemplate` used directly in every controller
6. PDF is stored but never parsed
7. PII stored in plain text
8. MD5 passwords
9. No transaction when creating an application
10. Session checks copy-pasted into every method

## What You Should Build

See `docs/v2-targets.md`.
