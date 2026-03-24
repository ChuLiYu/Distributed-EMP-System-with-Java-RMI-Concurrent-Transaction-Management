# FINTECH 專案決策記錄 (Decision Log)

> 專案：EMP (Employee Management Platform) - Fintech 商業級升級  
> 版本：v2.0  
> 記錄日期：2026-03-03

---

## 變更決策記錄

### 2026-03-03 - Phase 1: 核心安全與 RBAC 系統

#### Decision 1: 身份驗證與授權架構

**選項評估：**
1. **Session-based 傳統認證** - 適合傳統 Web 應用
2. **Token-based (JWT)** - 適合分散式/REST API
3. **RMI 上下文傳遞** - 適合現有 RMI 架構

**選擇：選項 3 - RMI 上下文傳遞**
- 理由：現有系統使用 RMI，保持向後兼容性
- UserId 和 RequestId 作為方法參數傳遞
- 在 Service 層統一處理稽核

**影響模組：** `EMPService`, `EMPServiceImpl`

---

#### Decision 2: 密碼雜湊演算法

**選項評估：**
1. **MD5** - 不安全，已淘汰
2. **SHA-256** - 標準，但速度過快容易遭受暴力攻擊
3. **BCrypt** - 業界標準，自帶 salt，成本可調

**選擇：BCrypt**
- 理由：SEC-001 要求安全的密碼儲存
- 符合 OWASP 密碼儲存建議
- 使用 Spring Security BCryptPasswordEncoder 或 JBcrypt

**影響模組：** `User`, `UserDAO`

---

#### Decision 3: RBAC 權限矩陣實作

**權限矩陣（依據 FINTECH_REQUIREMENTS.md）：**

| 角色       | 查詢 EMP | 新增 EMP | 修改 EMP | 刪除 EMP | 查看稽核 |
| ---------- | -------: | -------: | -------: | -------: | -------: |
| Viewer     |       ✅ |       ❌ |       ❌ |       ❌ |       ❌ |
| Operator   |       ✅ |       ✅ |       ✅ |       ❌ |       ❌ |
| Supervisor |       ✅ |       ✅ |       ✅ |       ✅ | ✅(只讀) |
| Admin      |       ✅ |       ✅ |       ✅ |       ✅ |       ✅ |

**選擇：權限檢查在 Service 層實現**
- 理由：符合最小權限原則
- 每個操作前檢查角色權限
- 拒絕時拋出 `AccessDeniedException`

**影響模組：** `PermissionChecker`, `EMPServiceImpl`

---

#### Decision 4: 稽核日誌儲存

**選項評估：**
1. **檔案日誌** - 簡單，但無法結構化查詢
2. **資料庫表** - 可查詢，符合稽核需求
3. **兩者兼有** - 推薦

**選擇：資料庫表 + 結構化日誌**
- 理由：AC-012 要求每筆稽核事件包含完整欄位
- 需要支援 future 查詢分析
- 稽核表欄位：
  - eventId (UUID)
  - eventTime (UTC)
  - actorId
  - actorRole
  - action
  - targetType
  - targetId
  - result
  - errorCode
  - requestId
  - sourceIp

**影響模組：** `AuditEvent`, `AuditDAO`

---

#### Decision 5: RequestId 追蹤機制

**選項評估：**
1. **手動傳遞** - 每個方法增加 requestId 參數
2. **ThreadLocal** - 上下文隔離，自動傳遞
3. **MDC (Mapped Diagnostic Context)** - Log4j/Logback 支援

**選擇：手動傳遞 + ThreadLocal 組合**
- 理由：RMI 需要明確的 requestId 傳遞
- ThreadLocal 儲存當前請求上下文
- 在 Service 方法入口記錄，異常時記錄

**影響模組：** `RequestContext`, `EMPServiceImpl`

---

## 技術實現摘要

### 新增類別

| 類別 | 職責 |
| ---- | ---- |
| `User` | 用戶實體 (id, username, passwordHash, role) |
| `Role` | 角色枚舉 (VIEWER, OPERATOR, SUPERVISOR, ADMIN) |
| `Permission` | 權限檢查工具類 |
| `AuditEvent` | 稽核事件實體 |
| `AuditDAO` | 稽核資料存取 |
| `RequestContext` | 請求上下文 (ThreadLocal) |
| `AuthService` | 認證服務 (login, hashPassword) |
| `AccessDeniedException` | 權限拒絕異常 |

### 修改類別

| 類別 | 變更 |
| ---- | ---- |
| `EMPService` | 新增 userId, requestId 參數 |
| `EMPServiceImpl` | 加入 RBAC 檢查與稽核記錄 |
| `EMPDAO` | 加入 requestId 參數傳遞 |

---

## 測試策略

### 單元測試
- `RolePermissionTest` - 權限矩陣驗證
- `AuditEventTest` - 稽核事件欄位驗證
- `PasswordHashTest` - 密碼雜湊驗證

### 整合測試
- `AuthIntegrationTest` - 登入流程
- `RbacIntegrationTest` - RBAC 權限檢查
- `AuditIntegrationTest` - 稽核日誌寫入

### 併發測試
- 現有 `ConcurrentTest` 保持通過

---

## 下一步行動

1. ✅ 創建決策記錄文檔
2. ⏳ 實現用戶/角色/RBAC系統
3. ⏳ 實現稽核日誌系統
4. ⏳ 實現密碼雜湊儲存
5. ⏳ 實現 requestId 追蹤
6. ⏳ 增強 EMPService 介面
7. ⏳ 實現 RBAC 權限檢查
8. ⏳ 運行測試驗證

---

*本文件將持續更新以記錄後續決策變更*
