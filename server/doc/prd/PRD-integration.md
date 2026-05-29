# PRD：外部集成模块（nanda-integration）

> **文档版本**：V1.0 · **状态**：Draft  
> **Maven 模块**：`nanda-integration`  
> **架构层**：横切集成层  
> **设计引用**：[FD-04 §19](../design/04-接口设计.md) · [FD-07](../design/07-模块功能设计.md)

---

## 1. 文档信息

| 项 | 内容 |
| --- | --- |
| 实现职责 | 外部系统对接 API 与适配器 |
| 关联 REQ | REQ-13-01-03、REQ-13-01-04（主责）；REQ-01-01-05、REQ-05-02-13（协同实现） |
| 独有 REQ 附录 | 2 |
| 优先级 | P0 / P1 |

---

## 2. 模块概述

封装与院外/院内外部系统的 **协议适配与 API 网关**，包括：分中心 Excel 上传、评估结果回写、HL7 FHIR R4 预留接口。

**原则**：集成层不持有核心业务状态；数据写入经 ingestion/governance 标准链路进入 Staging。

---

## 3. 目标用户与角色

| 角色 | 场景 |
| --- | --- |
| 分中心协调员 | 上传 Excel 模板数据 |
| 外部系统 | 接收评估报告回写、FHIR 查询（预留） |
| 数据管理员 | 配置对接凭证与映射 |

---

## 4. 范围定义

### 4.1 In Scope

- 分中心批量上传（Excel 模板校验 → Staging）
- 评估报告/风险结果回写 API
- FHIR R4 Patient/Observation 只读预留
- HL7/FHIR 适配器骨架（P1）

### 4.2 Out of Scope

- 核心业务 CRUD（归属各业务模块）
- 沙箱分析执行

### 4.3 一期交付

本模块主责 **2 REQ + 协同项一期全量**，含：分中心 upload、外部回传、FHIR R4 读接口、共病回传联动。

---

## 5. 关联 BR / REQ

| BR | REQ | 主责/协同 |
| --- | --- | --- |
| BR-01-01 | REQ-01-01-05 | 协同（ingestion 需求，integration 实现 API） |
| BR-05-05 | REQ-05-02-13 | 协同（asset 业务，integration 实现 writeback） |
| BR-13-01 | REQ-13-01-03、04 | **主责** |

---

## 6. 功能需求详述

### 6.1 分中心上传（REQ-01-01-05）

**用户故事**：作为**分中心协调员**，我希望按模板上传 Excel，以便数据进入中心 Staging 参与治理。

| 规则 | 说明 |
| --- | --- |
| 模板 | 预置字段与字典校验 |
| 流程 | upload → 解析 → stg_batch → 触发清洗 |
| 权限 | 仅本分中心 org_id |
| 失败 | 行级错误报告，支持部分成功 |

**接口**：`POST /api/v1/integration/upload`（FD-04 §19）

### 6.2 外部数据回写（REQ-13-01-03、REQ-05-02-13）

**用户故事**：作为**外部系统**，我希望接收平台推送的评估摘要，以便回写至院内系统。

| 规则 | 说明 |
| --- | --- |
| 内容 | 脱敏后的评估结论、风险分层，非原始明细 |
| 认证 | 双向 TLS 或 API Key + IP 白名单 |
| 重试 | 失败 3 次指数退避 |

**接口**：`POST /api/v1/integration/writeback`

### 6.3 FHIR 对接预留（REQ-13-01-04）

| 规则 | 说明 |
| --- | --- |
| MVP | 接口契约 + Mock；不生产启用 |
| P1 | Patient、Observation GET |
| 库 | HAPI FHIR 5.x |

**接口**：`/api/v1/integration/fhir/*`

---

## 7. 页面与交互

| 页面 | 说明 |
| --- | --- |
| 分中心上传 | 模板下载、文件选择、上传进度、错误行列表 |
| 对接配置（管理员） | 回写 URL、证书、FHIR baseUrl |

---

## 8. 接口需求

FD-04 §19 全文：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/v1/integration/upload` | 分中心 Excel 上传 |
| GET | `/api/v1/integration/upload/templates/{type}` | 下载模板 |
| POST | `/api/v1/integration/writeback` | 评估结果回写 |
| GET | `/api/v1/integration/fhir/Patient/{id}` | FHIR 预留 |
| GET | `/api/v1/integration/fhir/Observation` | FHIR 预留 |

---

## 9. 数据需求

- 不单独建业务主表；写入 `stg_*` 或调用 asset/analytics 服务
- 对接配置表：`int_endpoint_config`（建议）

---

## 10. 流程依赖

| 流程 | 职责 |
| --- | --- |
| BP-01 | 上传数据进入 Staging |
| BP-04 | 导出/报告触发 writeback |

---

## 11. 安全与权限

- 外部接口独立鉴权（非用户 JWT）
- 传输：HTTPS + 双向认证（REQ-05-07-08）
- 审计：所有 writeback 记录 audit_log

---

## 12. 非功能需求

| 指标 | 要求 |
| --- | --- |
| 上传 | 单文件 50MB，10 万行内 10min 解析 |
| 幂等 | upload 支持 client_request_id 防重 |

---

## 13. 模块依赖

| 模块 | 说明 |
| --- | --- |
| ingestion | Staging 写入 |
| asset | 共病回传业务校验 |
| analytics | 报告数据来源 |
| platform | 审计、外部认证 |

---

## 14. 验收标准

| 编号 | 通过标准 |
| --- | --- |
| AC-INT-01 | Excel 上传解析成功并产生 stg_batch |
| AC-INT-02 | 错误模板拒绝上传并提示行号 |
| AC-INT-03 | writeback 调用成功记入审计 |
| AC-INT-04 | FHIR 预留接口返回契约示例（P1） |

---

## 15. 里程碑建议

| 批次 | 内容 |
| --- | --- |
| M1 | upload + 模板 |
| M2 | writeback |
| M3 | FHIR 读接口 |

---

## 附录 A：REQ 清单（主责 2 项）

| REQ 编号 | 功能项 | 优先级 | 备注 |
| --- | --- | --- | --- |
| REQ-13-01-03 | 外部数据回传接口 | P1 | 主责 |
| REQ-13-01-04 | HL7 FHIR 对接预留 | P1 | 主责 |

## 附录 B：协同实现 REQ

| REQ 编号 | 功能项 | 主责模块 |
| --- | --- | --- |
| REQ-01-01-05 | 多中心支持 | ingestion（需求）/ integration（API） |
| REQ-05-02-13 | 共病库-外部回传 | asset（业务）/ integration（API） |
