# PRD：数据采集模块（nanda-ingestion）

> **文档版本**：V1.0 · **状态**：Draft  
> **Maven 模块**：`nanda-ingestion`  
> **架构层**：L2 数据治理层  
> **设计引用**：[FD-07 §7.1.1](../design/07-模块功能设计.md) · [FD-04 §3](../design/04-接口设计.md) · [FD-05 BP-01](../design/05-业务流程设计.md)

---

## 1. 文档信息

| 项 | 内容 |
| --- | --- |
| 覆盖 BR | BR-01-01 |
| REQ 数量 | 9（另 REQ-01-01-05 由 [integration](./PRD-integration.md) 实现上传 API） |
| 优先级 | P0 |

---

## 2. 模块概述

统一接入院内 HIS/LIS/PACS/EMR 及外部数据，按 **T+7 / T+1 / 准实时** 策略同步至 **Staging 层**，禁止直写 Published 专病库。

---

## 3. 目标用户与角色

| 角色 | 场景 |
| --- | --- |
| 数据管理员 | 配置数据源、同步任务、监控批次 |
| 分中心协调员 | 触发分中心文件上传（经 integration） |
| 系统管理员 | 全局数据源审批 |

---

## 4. 范围定义

### 4.1 In Scope

- 多协议适配器（HL7/FHIR/JDBC/API/文件）
- 数据源与 SyncJob 管理
- Staging 批次与原始记录
- T+7、T+1、NEAR_RT 调度

### 4.2 Out of Scope

- 清洗、EMPI、入库（governance/asset）
- CRF 手工录入

### 4.3 一期交付

本模块 **9 REQ 一期全量交付**，含：JDBC/文件、T+1/T+7、历史批量、HL7 v2、准实时 Webhook、DICOM、Staging 监控。

---

## 5. BR / REQ 覆盖

| BR | REQ |
| --- | --- |
| BR-01-01 | REQ-01-01-01 ~ REQ-01-01-09 |

---

## 6. 功能需求详述

### BR-01-01 多源数据采集与对接

**用户故事**

- 作为**数据管理员**，我希望配置 HIS 数据源并设置 T+1 同步，以便每日自动拉取诊疗数据到 Staging。
- 作为**数据管理员**，当同步失败时，我希望查看批次日志并重试，以便快速恢复采集。

| 功能点 | REQ | 优先级 | 设计要点 |
| --- | --- | --- | --- |
| 多源异构对接 | REQ-01-01-01 | P0 | 插件化 Adapter |
| 全维度临床采集 | REQ-01-01-02 | P0 | staging_record.domain 分类 |
| 外部 ETL 汇聚 | REQ-01-01-03 | P0 | 外部→Staging，禁止直写 Published |
| 历史数据采集 | REQ-01-01-04 | P1 | 批量导入+预审 |
| 多中心支持 | REQ-01-01-05 | P0 | API 见 integration 模块 |
| 同步调度机制 | REQ-01-01-06 | P0 | SyncJob + XXL-JOB |
| T+7 | REQ-01-01-07 | P1 | 周级 Cron |
| T+1 | REQ-01-01-08 | P0 | 日级增量 |
| 准实时 | REQ-01-01-09 | P2 | Webhook/日志监听 |

**异常**（BP-01）：连接失败→Failed+告警；解析错误→PARSE_ERROR；支持批次重试。

---

## 7. 页面与交互

| 页面 | 说明 |
| --- | --- |
| 数据源管理 | 列表、测试连接、协议类型 |
| 同步任务 | Cron 配置、立即执行、暂停/恢复 |
| 批次监控 | 批次状态、成功/失败数、下钻记录 |
| 批次详情 | 原始 JSON、错误原因、重试按钮 |

---

## 8. 接口需求

| FD-04 | 路径 |
| --- | --- |
| §3.1 | `/api/v1/datasources/*` |
| §3.2 | `/api/v1/sync-jobs/*` |
| §3.3 | `/api/v1/staging/*` |

---

## 9. 数据需求

| 前缀 | 实体 |
| --- | --- |
| `stg_` | stg_batch, stg_record, stg_datasource, stg_sync_job |

数据流：外部/HIS → Staging →（事件）→ governance 清洗。

---

## 10. 流程依赖

| 流程 | 职责 |
| --- | --- |
| BP-01 | 采集起点；输出 StagingBatch |
| BP-02 | 不经过本模块（CRF 直写 governance） |

---

## 11. 安全与权限

| 权限码 | 说明 |
| --- | --- |
| `ingestion:datasource:read/write` | 数据源 |
| `ingestion:sync:execute` | 执行任务 |
| `ingestion:staging:read` | 查看批次 |

数据权限：本机构及下级（数据管理员）。

---

## 12. 非功能需求

| 指标 | 要求 |
| --- | --- |
| 单日增量 | 支持百万级 staging_record（分批提交） |
| 任务失败 | 邮件/站内信告警 |
| 重试 | 指数退避，最多 3 次 |

---

## 13. 模块依赖

| 上游 | platform（认证、审计） |
| 下游 | governance（清洗触发） |
| 协同 | integration（分中心上传） |
| 中间件 | XXL-JOB、MySQL |

---

## 14. 验收标准

| 编号 | 通过标准 |
| --- | --- |
| AC-I-01 | HIS/LIS JDBC 联调，批次 SUCCESS |
| AC-I-02 | T+1 Cron 可配置并自动执行 |
| AC-I-03 | 失败批次可重试 |
| AC-I-04 | 无直写 pub_* 表的路径 |

---

## 15. 里程碑建议

| 批次 | 内容 |
| --- | --- |
| M1 | 数据源 + Staging 表 + 手动触发 |
| M2 | XXL-JOB T+1 + 监控页 |
| M3 | HL7 适配器（P1） |

---

## 附录 A：REQ 清单（9 项）

| REQ 编号 | 功能项 | 优先级 |
| --- | --- | --- |
| REQ-01-01-01 | 多源异构数据对接 | P0 |
| REQ-01-01-02 | 全维度临床数据采集 | P0 |
| REQ-01-01-03 | 外部数据整合汇聚 | P0 |
| REQ-01-01-04 | 历史数据采集 | P1 |
| REQ-01-01-05 | 多中心支持 | P0 |
| REQ-01-01-06 | 数据同步调度机制 | P0 |
| REQ-01-01-07 | 常规周期适配（T+7） | P1 |
| REQ-01-01-08 | 高频周期适配（T+1） | P0 |
| REQ-01-01-09 | 准实时周期适配 | P2 |
