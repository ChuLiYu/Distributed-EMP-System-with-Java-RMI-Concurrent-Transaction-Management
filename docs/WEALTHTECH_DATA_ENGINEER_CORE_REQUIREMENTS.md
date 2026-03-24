# WealthTech + Data Engineer 核心練習需求規格（Core Practice v1）

> 版本：v1.0  
> 更新日期：2026-03-03  
> 基底文件：FINTECH_REQUIREMENTS.md（v2）  
> 適用分支：`fintech`

---

## 1. 文件目的

本文件將原本「商業級 Fintech 全面需求」收斂為「WealthTech + Data Engineer 核心能力練習版」，聚焦可在目前專案中落地的內容。

### 1.1 這版只做什麼

- 只練 **核心資料工程能力** + **WealthTech 場景思維**。
- 以可驗收、可演示、可測試為主，不追求完整企業上線能力。

### 1.2 這版不做什麼

- 不做完整 KYC 供應商串接、正式法規認證。
- 不做完整 IAM 平台、完整 SIEM、跨區災備實作。

---

## 2. 角色定位：WealthTech Data Engineer 的核心職責

## DE-ROLE-001 資料生命週期管理

- 設計並維護資料流：`來源 → 清洗 → 結構化 → 服務層查詢`。
- 確保資料品質（完整性、唯一性、一致性、可追溯）。

## DE-ROLE-002 交易資料可靠性

- 在寫入流程中正確實作交易邊界（commit/rollback）。
- 確保錯誤不會造成半套資料。

## DE-ROLE-003 併發與一致性

- 能解釋與驗證併發衝突行為（例如 SQLite locked/busy）。
- 能提出資料庫與架構升級建議（從 SQLite 到 PostgreSQL）。

## DE-ROLE-004 可觀測與稽核資料準備

- 定義最小稽核欄位與追蹤欄位（requestId, actorId, action, result）。
- 讓資料可被後續風控、合規與分析消費。

---

## 3. 專案目標（調整後：只練核心）

## 3.1 領域優先順序

- **Priority 1：WealthTech（核心主軸）**
  - 聚焦投資/資產資料正確性、可追溯性與分析可用性。
- **Priority 2：Payment（次主軸）**
  - 納入「支付清算 MVP」流程，作為交易一致性與資料工程延伸練習。

## GOAL-001 核心資料服務

- 完成穩定的核心領域資料服務（本地 + RMI）。
- 保證資料寫入交易一致性。

## GOAL-002 核心資料工程能力

- 完成資料品質約束（主鍵唯一、欄位驗證、錯誤處理）。
- 完成最小稽核事件欄位定義（先 schema/格式，不必全鏈上雲）。

## GOAL-003 核心併發驗證

- 完成併發讀寫測試並產出統計（成功率、衝突率、最終一致性）。
- 形成「現況限制 + 升級方向」技術結論。

## GOAL-004 支付清算 MVP（第二優先）

- 建立最小支付清算資料流程：`支付指令 -> 清算事件 -> 對帳狀態`。
- 以交易一致性為核心，驗證「不可重複清算、可回滾、可追蹤」。

---

## 4. 核心需求（CR）與驗收條件（AC）

## CR-001 交易一致性（核心必做）

### 需求

- 所有寫入操作需在手動交易模式下執行。
- 成功 commit，失敗 rollback。

### AC

- AC-001：重複主鍵寫入後，資料狀態維持一致（無部分成功）。
- AC-002：交易失敗時，錯誤需被記錄且可定位。
- AC-003：測試可重現 commit/rollback 路徑。

## CR-002 資料品質（核心必做）

### 需求

- 關鍵欄位需有驗證策略（not null、長度、格式）。

### AC

- AC-004：非法輸入不進資料庫。
- AC-005：錯誤回應含可追蹤訊息（至少 error code 或 message）。

## CR-003 併發可觀測（核心必做）

### 需求

- 多客戶端並行讀寫，收斂出可量化結果。

### AC

- AC-006：輸出成功率、失敗率、衝突次數、最終筆數差異。
- AC-007：可展示至少 1 種併發衝突樣態（busy/locked）。

## CR-004 最小稽核模型（核心必做）

### 需求

- 定義並產生最小稽核事件（可先落檔或 console 結構化輸出）。

### AC

- AC-008：每筆事件至少包含 `eventTime`, `actorId`, `action`, `targetId`, `result`, `requestId`。
- AC-009：可用時間區間與 action 過濾事件。

## CR-005 資料工程輸出（核心必做）

### 需求

- 產出「資料血緣 + 資料字典 + 測試報告」三份核心 artefacts。

### AC

- AC-010：有明確資料來源與去向圖（文字或圖皆可）。
- AC-011：核心領域欄位字典可被查閱（至少含 Portfolio/Holding/Payment）。
- AC-012：測試報告可證明核心需求達成。

## CR-006 支付清算 MVP（第二優先）

### 需求

- 建立最小清算模型，至少包含：
  - `payment_id`
  - `status`（PENDING/CLEARED/FAILED/REVERSED）
  - `amount`
  - `currency`
  - `clearing_ref`
  - `request_id`
- 清算流程需具備冪等保護，避免同一支付重複清算。

### AC

- AC-013：同一 `payment_id` 重送不會造成重複清算結果。
- AC-014：清算失敗可回滾，狀態維持一致。
- AC-015：可產出「支付清算事件明細」供對帳檢查。

---

## 5. 技術範圍收斂（以練習為目的）

## In Scope

- Java + JDBC + SQLite
- Java RMI（作為分散式概念載體）
- JUnit 測試（交易、併發、資料一致性）
- 支付清算 MVP（狀態流轉、冪等、防重複清算、對帳輸出）
- 文件化（需求、驗收、結論）

## Out of Scope

- 正式 OAuth2/OIDC 平台整合
- 真實支付網路（卡組織/銀行清算中心）正式串接
- 大規模串流平台（Kafka/Flink）正式部署

---

## 6. 與現有專案對應（Practice Mapping）

- 目前程式基礎（過渡層）：
  - `EMPService`, `EMPServiceImpl`, `EMPDAO`, `DBConnection`
- 目標核心領域（應新增）：
  - `PortfolioService`, `HoldingService`, `PaymentClearingService`
  - `PortfolioDAO`, `HoldingDAO`, `PaymentDAO`, `AuditDAO`
- 核心驗證：
  - `TransactionTest`, `ConcurrentTest`（沿用）
  - `PaymentClearingTest`, `ReconciliationTest`（新增）

> 註：EMP 目前僅作為練習資料集與技術載體，不代表 WealthTech/PayTech 業務核心。

---

## 7. 量化驗收門檻（核心版）

- 測試門檻：
  - `RmiTest`, `TransactionTest`, `ConcurrentTest` 全綠。
- 交易門檻：
  - 重複鍵錯誤可 rollback，且資料無破壞。
- 併發門檻：
  - 提供至少一份包含成功率/衝突率/最終一致性的測試輸出。
- 文件門檻：
  - 本文件 + 資料字典 + 測試摘要完整。

---

## 8. 實作優先順序（只練核心）

## P1（必做）

1. 交易一致性（CR-001）
2. 併發可觀測（CR-003）
3. 核心測試可重現（Rmi/Transaction/Concurrent）

## P2（應做）

1. 最小稽核模型（CR-004）
2. 資料品質驗證（CR-002）
3. 支付清算 MVP（CR-006）

## P3（可做）

1. 資料血緣圖與欄位字典完善（CR-005）
2. PostgreSQL 升級 PoC 設計稿

---

## 9. 核心練習交付物（Deliverables）

1. 可執行程式碼（`fintech` 分支）
2. 測試結果截圖/文字（三組核心測試）
3. 本需求文件（核心版）
4. 一頁技術結論：
   - 現況限制（例如 SQLite 單寫鎖）
   - 升級建議（PostgreSQL + API 化）
5. 支付清算 MVP 輸出：

- 狀態流轉定義
- 防重複清算（冪等）驗證結果
- 對帳事件摘要

---

## 10. 一句話總結（給口試）

本專案在 WealthTech + Data Engineer 核心練習中，以 WealthTech 為第一優先、Payment 為第二優先，重點是把「資料正確性、交易一致性、併發可觀測、支付清算 MVP」做到可證明、可解釋、可擴充。

---

## 11. 模組化架構設計（可並行開發版）

本章定義可拆分給多個 AI agent 並行實作的模組邊界。

## 11.1 模組清單

- **M01 - Core Domain Module（核心資料域）**
  - 內容：Portfolio、Holding、TradeOrder、Payment、ClearingEvent、AuditEvent 等資料模型與 enum。
  - 目標：統一定義資料結構與狀態，不含基礎設施邏輯。

- **M02 - Repository Module（資料存取層）**
  - 內容：PortfolioDAO（新增）、HoldingDAO（新增）、PaymentDAO（新增）、AuditDAO（新增）。
  - 目標：封裝 SQL、交易邊界、資源釋放。

- **M03 - Service Module（業務服務層）**
  - 內容：PortfolioService（新增）、HoldingService（新增）、PaymentClearingService（新增）。
  - 目標：實作業務規則（冪等、防重複清算、狀態流轉）。

- **M04 - Interface Module（介面層）**
  - 內容：RMI 對外接口與 Console/Client 呼叫入口。
  - 目標：暴露可用 API，不放 SQL 與交易細節。

- **M05 - Audit & Observability Module（稽核與可觀測）**
  - 內容：requestId、結構化日誌、最小稽核事件落地。
  - 目標：支援追蹤與驗收證據輸出。

- **M06 - Test Harness Module（測試模組）**
  - 內容：RmiTest、TransactionTest、ConcurrentTest、PaymentClearingTest（新增）。
  - 目標：以測試驗證需求（AC）達成。

## 11.2 模組依賴規則

- 允許依賴：`M04 -> M03 -> M02 -> M01`
- 稽核為橫切能力：`M05` 可被 `M03/M04` 使用
- 測試依賴：`M06` 可依賴所有模組
- 禁止：
  - `M02` 依賴 `M04`
  - `M01` 依賴任何上層模組

---

## 12. 模組互動與時序（核心流程）

## 12.1 EMP 寫入流程（交易一致性）

## 12.1 Portfolio/Holding 寫入流程（交易一致性）

1. Interface 層接收請求（含 requestId、actorId）。
2. Service 層執行驗證規則。
3. Repository 開啟交易（manual commit）。
4. SQL 成功 -> commit；失敗 -> rollback。
5. Audit 模組記錄結果（SUCCESS/FAIL）。
6. 回傳結果與 requestId。

## 12.2 Payment 清算流程（MVP）

1. 接收清算請求：`payment_id`, `amount`, `currency`, `request_id`。
2. Service 先做冪等檢查：
   - 若 `payment_id` 已 CLEARED，直接回傳既有結果（不得重複清算）。
3. 狀態流轉：`PENDING -> CLEARED` 或 `PENDING -> FAILED`。
4. 交易成功 commit，並產生 `clearing_ref`。
5. 記錄 ClearingEvent + AuditEvent。

## 12.3 對帳摘要流程

1. 依時間區間抓取 ClearingEvent。
2. 聚合：成功筆數、失敗筆數、總金額。
3. 輸出對帳摘要（CSV/console 皆可）。

---

## 13. 資料模型與狀態機定義

## 13.1 Payment 狀態機（MVP）

- 初始：`PENDING`
- 可轉移：
  - `PENDING -> CLEARED`
  - `PENDING -> FAILED`
  - `CLEARED -> REVERSED`（可選，若本期實作）
- 禁止：
  - `CLEARED -> PENDING`
  - `FAILED -> CLEARED`（需新交易，而非原交易覆寫）

## 13.2 建議資料表（最小）

- `PORTFOLIO`：`portfolio_id`, `owner_id`, `base_currency`, `status`, `created_at`, `updated_at`
- `HOLDING`：`portfolio_id`, `asset_symbol`, `quantity`, `avg_cost`, `updated_at`
- `TRADE_ORDER`：`order_id`, `portfolio_id`, `asset_symbol`, `side`, `qty`, `price`, `status`, `request_id`, `created_at`

- `PAYMENT`：`payment_id`, `amount`, `currency`, `status`, `created_at`, `updated_at`, `request_id`
- `CLEARING_EVENT`：`event_id`, `payment_id`, `from_status`, `to_status`, `clearing_ref`, `event_time`, `request_id`
- `AUDIT_EVENT`：`event_id`, `actor_id`, `action`, `target_type`, `target_id`, `result`, `error_code`, `request_id`, `event_time`

---

## 14. 接口契約（給 AI Agent 的固定邊界）

## 14.1 Service 介面契約（草案）

- `createPayment(paymentId, amount, currency, requestId, actorId)`
- `clearPayment(paymentId, requestId, actorId)`
- `getPaymentById(paymentId)`
- `getClearingSummary(fromTime, toTime)`

## 14.2 錯誤碼契約（草案）

- `PAYMENT_ALREADY_CLEARED`
- `PAYMENT_NOT_FOUND`
- `INVALID_PAYMENT_STATE`
- `VALIDATION_ERROR`
- `DB_LOCKED`
- `DB_CONSTRAINT_VIOLATION`

## 14.3 冪等契約

- 相同 `payment_id + request_id` 重送，結果必須一致。
- 相同 `payment_id` 重送清算，不得產生第二筆 CLEARED 事件。

---

## 15. AI Agent 並行開發切分（Workstream）

## WS-A（Domain + Schema）

- 範圍：M01 + 資料表 migration/DDL。
- 產出：Domain class、enum、DDL 文件。
- 不可修改：Service 業務流程。

## WS-B（Repository）

- 範圍：M02。
- 產出：PaymentDAO、AuditDAO、交易處理。
- 依賴：WS-A 先定義 schema。

## WS-C（Service）

- 範圍：M03。
- 產出：PaymentClearingService、狀態機與冪等規則。
- 依賴：WS-A + WS-B。

## WS-D（Interface/RMI）

- 範圍：M04。
- 產出：RMI 介面擴充、Client 呼叫入口。
- 依賴：WS-C 完成 service 契約。

## WS-E（Audit/Observability）

- 範圍：M05。
- 產出：requestId 注入、AuditEvent 寫入、結構化輸出。
- 依賴：可與 WS-B、WS-C 併行，最後整合。

## WS-F（Testing）

- 範圍：M06。
- 產出：PaymentClearingTest、IdempotencyTest、ReconciliationTest。
- 依賴：WS-B/C/D。

---

## 16. 並行整合順序（DAG）

- Step 1：WS-A
- Step 2：WS-B + WS-E（可平行）
- Step 3：WS-C
- Step 4：WS-D + WS-F（可平行）
- Step 5：整體回歸測試 + 文件更新

---

## 17. 每個模組的 Definition of Done（DoD）

## M01 DoD

- Domain class 與 enum 完成，欄位命名與文件一致。
- 無循環依賴。

## M02 DoD

- 每個寫入方法具備 `commit/rollback/finally close`。
- 出錯回傳可追蹤錯誤碼或訊息。

## M03 DoD

- 狀態機檢查完整。
- 冪等規則由測試覆蓋。

## M04 DoD

- Interface 不包含 SQL。
- 所有對外方法包含 requestId。

## M05 DoD

- AuditEvent 欄位完整。
- 失敗路徑也有稽核紀錄。

## M06 DoD

- 新增測試全綠。
- 原有 `RmiTest`、`TransactionTest`、`ConcurrentTest` 不退化。

---

## 18. Agent 任務模板（可直接分派）

每個 agent 任務單需包含：

1. **目標模組**：例如 `WS-B Repository`
2. **可修改檔案白名單**：限制修改範圍，避免衝突
3. **輸入契約**：依賴哪些介面/資料模型
4. **輸出契約**：新增哪些 class、方法、測試
5. **驗收命令**：例如 `mvn test -Dtest=PaymentClearingTest`
6. **禁止事項**：不得改動他組白名單檔案

---

## 19. 核心版最終驗收（Project Gate）

- Gate-1：核心測試全綠（含新增 Payment 清算測試）。
- Gate-2：清算冪等驗證通過（無重複 CLEARED）。
- Gate-3：對帳摘要輸出可重現。
- Gate-4：文件與程式一致（欄位、狀態、錯誤碼）。
