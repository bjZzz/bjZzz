# PRD 总览

> **文档版本**：V1.0  
> **状态**：Draft  
> **上游**：[功能设计总览](../design/00-功能设计总览.md) · [技术架构推荐](../design/08-技术架构推荐.md)  
> **追溯**：30 BR / 233 REQ

---

## 1. 文档说明

本目录为共病专病科研平台的**模块级产品需求文档（PRD）**，在功能设计（FD-00~08）基础上，面向产品、研发、测试提供可排期、可验收的需求说明。

| 读者 | 用途 |
| --- | --- |
| 产品 | 版本规划、验收标准、跨模块协调 |
| 研发 | 模块边界、功能范围、接口与数据依赖 |
| 测试 | 用例设计、REQ 覆盖矩阵 |

**技术基线**（详见 [08-技术架构推荐](../design/08-技术架构推荐.md)）：Spring Boot 2.7.x · Java 8 · MySQL 8.0 · 模块化单体。

**不单独出 PRD 的工程模块**：`nanda-common`（公共组件）、`nanda-boot`（启动聚合），在各模块 PRD 中按需引用。

---

## 2. 追溯关系

```
功能要求.md / 功能需求清单.md (REQ-*)
        ↓
业务功能需求.md (BR-*)
        ↓
doc/design/ (FD-00~08 功能设计)
        ↓
doc/prd/ (本 PRD 体系)  ← 当前
        ↓
doc/detail/ (详细设计 LLD)
        ↓
（后续）Flyway DDL / Maven 工程 / 代码实现
```

| 层级 | 编号 | PRD 中的体现 |
| --- | --- | --- |
| 功能需求 | REQ-* | 各模块附录 REQ 表 |
| 业务需求 | BR-* | §5、§6 按 BR 组织 |
| 功能设计 | FD-* | 引用链接，不重复全文 |
| 产品需求 | PRD-* | 用户故事、页面、验收清单 |

---

## 3. 模块 PRD 索引

| PRD | Maven 模块 | 架构层 | BR 数 | REQ 数 | 优先级 | 文档 |
| --- | --- | --- | ---: | ---: | --- | --- |
| 平台基础 | `nanda-platform` | L1 | 2 | 16 | P0 | [PRD-platform](./PRD-platform.md) |
| 数据采集 | `nanda-ingestion` | L2 | 1 | 9 | P0 | [PRD-ingestion](./PRD-ingestion.md) |
| 数据治理 | `nanda-governance` | L2 | 4 | 43 | P0 | [PRD-governance](./PRD-governance.md) |
| 数据资产 | `nanda-asset` | L3 | 11 | 57 | P0/P1 | [PRD-asset](./PRD-asset.md) |
| 科研协作 | `nanda-research` | L5 | 4 | 22 | P0/P1 | [PRD-research](./PRD-research.md) |
| 分析应用 | `nanda-analytics` | L4 | 9 | 84 | P0~P2 | [PRD-analytics](./PRD-analytics.md) |
| 外部集成 | `nanda-integration` | 横切 | — | 2 | P0/P1 | [PRD-integration](./PRD-integration.md) |

> integration 附录 A 为 2 项主责 REQ；REQ-01-01-05、REQ-05-02-13 见附录 B 协同项（计入 ingestion/asset 附录）。

**REQ 合计（附录 A 去重）**：16 + 9 + 43 + 57 + 22 + 84 + 2 = **233**

---

## 4. 全局角色矩阵

| 角色 | platform | ingestion | governance | asset | research | analytics | integration |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 系统管理员 | ● | ● | ● | ● | ● | ● | ● |
| 数据管理员 | ○ | ● | ● | ○ | ○ | ○ | ○ |
| 质控审核员 | — | — | ○ | ● | ○ | — | — |
| 科研项目负责人 | — | — | ○ | ○ | ● | ● | — |
| 科研人员 | — | — | ○ | ○ | ○ | ● | — |
| 临床录入员 | — | — | ● | ● | ● | — | — |
| 分中心协调员 | — | ○ | — | ○ | — | — | ● |

> ● 主要使用 ○ 次要/只读 — 无直接权限。详见 [FD-06](../design/06-安全与权限设计.md) §2。

---

## 5. 端到端流程与模块触点

| 流程 | 名称 | 主责模块 | 协同模块 |
| --- | --- | --- | --- |
| BP-01 | 多源采集与入库 | ingestion → governance → asset | platform, integration |
| BP-02 | CRF 录入与补录 | governance, asset | platform |
| BP-03 | 质控复核闭环 | asset | governance, platform |
| BP-04 | 检索与数据导出 | analytics | asset, platform, research |
| BP-05 | 科研队列与随访 | research | governance, analytics, asset |
| BP-06 | 沙箱分析 | analytics, sandbox | platform, asset |
| BP-07 | 风险评估与评估报告 | analytics, sandbox | asset, platform |
| BP-08 | 外部集成与结果回传 | integration | analytics, asset, platform |
| BP-09 | 知识库与患者全景 | asset | platform |

详见 [05-业务流程设计](../design/05-业务流程设计.md)。

---

## 6. 一期交付范围（全量）

对齐 [FD-10 一期实施设计](../design/10-分期实施设计.md) 与 [08-技术架构推荐](../design/08-技术架构推荐.md) §10：

| 模块 | 一期交付 |
| --- | --- |
| platform | 机构/用户/RBAC/JWT/审计/加密 L0~L3、共病 L3 审批 |
| ingestion | JDBC/文件/HL7 + T+1/T+7 + 历史批量 + 准实时 + DICOM |
| governance | CRF + 字典/清洗/发布 + 元数据血缘 |
| asset | EMPI + **三类专病** + 共病库 + 知识库 + 驾驶舱/360 + 质控/补录 |
| research | 项目 + 队列 + 随访 + 多项目协作 |
| analytics | ES 检索 + 风险/PDF + 统计/业务分析/仪表盘 + CDISC + K8s 沙箱 |
| integration | 分中心 upload + FHIR R4 + writeback |

**REQ 覆盖**：233 项全量一期交付。PRD 附录 P0/P1/P2 表示需求重要度，不表示实施分期。

---

## 7. 统一 PRD 章节说明

各模块 PRD 采用统一 15 章结构：文档信息 → 概述 → 角色 → 范围 → BR/REQ → 功能详述 → 页面 → 接口 → 数据 → 流程 → 安全 → NFR → 依赖 → 验收 → 里程碑 → 附录 REQ。

---

## 8. 文档维护

| 变更类型 | 更新顺序 |
| --- | --- |
| 新增 REQ | 功能需求清单 → FD-07 附录 A → 对应 PRD 附录 |
| 新增 BR | 业务功能需求 → FD-07 章节 → 对应 PRD §6 |
| 接口变更 | FD-04 → PRD §8 |

---

## 9. 修订记录

| 版本 | 日期 | 说明 |
| --- | --- | --- |
| V1.0 | 2026-05-27 | 初版，8 模块 PRD + 总览 |

---

## 10. REQ 覆盖校验

| 模块 PRD | 附录 A REQ 数 |
| --- | ---: |
| PRD-platform | 16 |
| PRD-ingestion | 9 |
| PRD-governance | 43 |
| PRD-asset | 57 |
| PRD-research | 22 |
| PRD-analytics | 84 |
| PRD-integration | 2 |
| **合计** | **233** |

协同实现（不计入上表重复项）：REQ-01-01-05（ingestion + integration API）、REQ-05-02-13（asset + integration API）。
