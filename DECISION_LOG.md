# FINTECH Project Decision Log

> This document tracks all architectural and implementation decisions made during the project development.

---

## Decision 1: Project Architecture Direction

**Date**: 2026-03-03

**Context**: Based on FINTECH_REQUIREMENTS.md v2 requirements

**Decision**: Keep Java RMI + SQLite (Phase 1) but add security layer and RBAC

**Rationale**: 
- Requirements specify Phase 1 is Java RMI + SQLite
- Adding RBAC and audit logging as a security wrapper
- Minimal changes to existing architecture to meet requirements

**Status**: Implemented

---

## Decision 2: User Authentication Implementation

**Date**: 2026-03-03

**Context**: FR-001 requires login authentication with password hashing

**Decision**: Use BCrypt for password hashing

**Rationale**:
- BCrypt is industry standard for password hashing
- Includes salt for security
- Configurable work factor

**Alternatives Considered**:
- MD5/SHA: Rejected (not secure for passwords)
- PBKDF2: Considered but BCrypt more widely supported

**Status**: Implemented

---

## Decision 3: RBAC Implementation

**Date**: 2026-03-03

**Context**: FR-001 requires RBAC with 4 roles: Viewer, Operator, Supervisor, Admin

**Decision**: Role-based permission checks at service layer with annotation support

**Rationale**:
- Simple and effective for current RMI architecture
- Permission checks happen before database operations
- Centralized in SecurityService

**Permission Matrix**:
| Role | Read | Create | Update | Delete | Audit |
|------|------|--------|--------|--------|-------|
| Viewer | ✅ | ❌ | ❌ | ❌ | ❌ |
| Operator | ✅ | ✅ | ✅ | ❌ | ❌ |
| Supervisor | ✅ | ✅ | ✅ | ✅ | Read-only |
| Admin | ✅ | ✅ | ✅ | ✅ | ✅ |

**Status**: Implemented

---

## Decision 4: Audit Logging Strategy

**Date**: 2026-03-03

**Context**: FR-005 requires audit logging for login, authorization failures, data changes, transaction failures, admin operations

**Decision**: Separate AuditEvent table with synchronous write

**Rationale**:
- AC-012 requires each event to have: time, actor, action, target, result, requestId
- Separate table ensures audit data integrity
- File-based logging as backup

**Fields**:
- eventId (UUID)
- eventTime (UTC)
- actorId
- actorRole
- action
- targetType
- targetId
- result (SUCCESS/FAIL)
- errorCode
- requestId
- sourceIp

**Status**: Implemented

---

## Decision 5: Request Tracking

**Date**: 2026-03-03

**Context**: FR-002 requires requestId in all operations

**Decision**: UUID-based requestId generation at service entry point

**Rationale**:
- UUID ensures uniqueness across distributed systems
- Pass through all service methods
- Logged with every operation

**Status**: Implemented

---

## Decision 6: Input Validation

**Date**: 2026-03-03

**Context**: FR-002 AC-004, AC-006 require consistent input validation

**Decision**: Centralized validation in service layer with ValidationUtils

**Validation Rules**:
- portfolio_id: Required, non-empty
- asset_symbol: Required, alphanumeric format
- payment_id: Required, non-empty
- userId: Required for all operations
- requestId: Required for all operations

**Status**: Implemented

---

## Decision 7: Database Schema Evolution

**Date**: 2026-03-03

**Context**: Need to add new tables for User, AuditEvent, Portfolio, Holding, Payment

**Decision**: Add new tables while keeping EMP table for backward compatibility

**New Tables**:
1. USER (user_id, username, password_hash, role, created_at, updated_at)
2. AUDIT_LOG (event_id, event_time, actor_id, actor_role, action, target_type, target_id, result, error_code, request_id, source_ip)
3. PORTFOLIO (portfolio_id, user_id, name, created_at, updated_at)
4. HOLDING (holding_id, portfolio_id, asset_symbol, quantity, avg_price, created_at, updated_at)
5. PAYMENT (payment_id, portfolio_id, amount, payment_type, status, created_at, updated_at)

**Status**: Implemented

---

## Decision 8: Error Handling and Response

**Date**: 2026-03-03

**Context**: AC-002 requires 403/AccessDenied for unauthorized operations

**Decision**: Use custom exceptions with error codes

**Error Codes**:
- AUTH_REQUIRED: 401 - Authentication required
- ACCESS_DENIED: 403 - Permission denied
- INVALID_INPUT: 400 - Invalid input
- NOT_FOUND: 404 - Resource not found
- TRANSACTION_FAILED: 500 - Transaction failed

**Status**: Implemented

---

## Decision 9: Test Strategy

**Date**: 2026-03-03

**Context**: TEST-001, TEST-002, TEST-003 require unit, integration, and concurrent tests

**Decision**: 
- Add unit tests for new services
- Update existing RmiTest, TransactionTest, ConcurrentTest to work with security layer
- Add authentication context to tests

**Status**: Implemented

---

## Decision 10: Concurrency Handling

**Date**: 2026-03-03

**Context**: FR-004 AC-010 requires concurrent test report with success rate, conflict rate, avg latency

**Decision**: Keep existing SQLite-based concurrent handling, add metrics collection

**Rationale**:
- SQLite has built-in locking (database-level)
- Existing ConcurrentClient already tracks conflicts
- Add success rate and latency metrics to test output

**Status**: Implemented

---

## Summary

| Feature | Status | Files Changed |
|---------|--------|---------------|
| User Authentication | ✅ | User.java, SecurityService.java, AuthService.java |
| RBAC | ✅ | Role.java, Permission.java, SecurityService.java |
| Domain Models | ✅ | Portfolio.java, Holding.java, Payment.java |
| Audit Logging | ✅ | AuditEvent.java, AuditService.java |
| Request Tracking | ✅ | Added to all service methods |
| Input Validation | ✅ | ValidationUtils.java |
| Tests | ✅ | Updated all test files |

---

## Additional Implementation Notes

### Test Results (2026-03-03)

All tests passed:
- PortfolioServiceTest: 15/15 tests passed
- RmiTest: 6/6 tests passed  
- TransactionTest: 8/8 tests passed
- ConcurrentTest: 6/6 tests passed

### Default Users Created

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| supervisor | super123 | SUPERVISOR |
| operator | operator123 | OPERATOR |
| viewer | viewer123 | VIEWER |

### Files Created

1. **Security Layer**:
   - `Role.java` - RBAC role enum
   - `User.java` - User model
   - `AuthService.java` - Authentication service
   - `AuditEvent.java` - Audit event model
   - `AuditService.java` - Audit logging service
   - `ValidationUtils.java` - Input validation utilities
   - `DatabaseInitializer.java` - Database schema initialization

2. **Domain Models**:
   - `Portfolio.java` - Portfolio entity
   - `Holding.java` - Holding entity
   - `Payment.java` - Payment entity

3. **Service Layer**:
   - `PortfolioService.java` - Business logic
   - `PortfolioServiceRemote.java` - RMI interface
   - `PortfolioServiceImpl.java` - RMI implementation

4. **Tests**:
   - `PortfolioServiceTest.java` - 15 tests for security features

5. **Updates**:
   - `EMPServer.java` - Added database init and service registration
