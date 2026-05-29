# API 与错误码规范

> **文档编号**：DD-03  
> **上游**：[FD-04 接口设计](../design/04-接口设计.md)

---

## 1. 统一响应

### 1.1 成功

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "requestId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 1.2 分页

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [],
    "page": 1,
    "size": 20,
    "total": 100
  },
  "requestId": "..."
}
```

### 1.3 错误

```json
{
  "code": 40001,
  "message": "参数校验失败",
  "errors": [{"field": "orgName", "reason": "不能为空"}],
  "requestId": "..."
}
```

---

## 2. HTTP 与业务码

| HTTP | 场景 | 业务 code 示例 |
| --- | --- | --- |
| 200 | 成功 | 0 |
| 201 | 创建 | 0 |
| 400 | 参数错误 | 40001 |
| 401 | 未登录/Token 失效 | 40101 |
| 403 | 无权限 | 40301 |
| 404 | 资源不存在 | 40401 |
| 409 | 冲突 | 40901 |
| 422 | 业务规则失败 | 42201 |
| 500 | 系统错误 | 50001 |

---

## 3. 错误码分段

| 段 | 模块 | 示例 |
| --- | --- | --- |
| 40001~40099 | 全局参数 | 40001 参数校验失败 |
| 40101~40199 | 认证 | 40101 Token 过期 |
| 40301~40399 | 授权 | 40301 无功能权限 |
| 41001~41099 | platform | 41001 机构编码重复 |
| 42001~42099 | ingestion | 42001 数据源连接失败 |
| 43001~43099 | governance | 43001 CRF 未发布 |
| 44001~44099 | asset | 44001 EMPI 合并冲突 |
| 45001~45099 | research | 45001 项目状态不允许 |
| 46001~46099 | analytics | 46001 导出待审核 |
| 47001~47099 | integration | 47001 上传模板不匹配 |
| 50001~50099 | 系统 | 50001 内部错误 |

---

## 4. 请求 Header

| Header | 必填 | 说明 |
| --- | --- | --- |
| Authorization | 除 login 外必填 | `Bearer {accessToken}` |
| X-Org-Id | 建议 | 当前操作机构 |
| X-Request-Id | 建议 | UUID，链路追踪 |

---

## 5. Java 类型映射

| API 类型 | Java 类型 | 校验 |
| --- | --- | --- |
| string | String | @NotBlank, @Size |
| integer | Integer/Long | @Min, @Max |
| number | BigDecimal | @Digits |
| boolean | Boolean | — |
| date | LocalDate | @Past, @Future |
| datetime | LocalDateTime | ISO 8601 |
| enum | Enum | @NotNull |
| array | List\<T\> | @NotEmpty |
| object | DTO | @Valid 嵌套 |

---

## 6. 分页 Query

| 参数 | 类型 | 默认 | 规则 |
| --- | --- | --- | --- |
| page | int | 1 | ≥1 |
| size | int | 20 | 1~100 |
| sort | string | — | `field,asc\|desc` |

---

## 7. 权限注解

```java
@PreAuthorize("hasAuthority('asset:specialty:patient:list')")
@DataScope(orgAlias = "o")  // MyBatis 注入 org_id 条件
@AuditLog(action = "QUERY", resourceType = "SPECIALTY_PATIENT")
```

---

## 8. 模块 API 详设索引

| FD-04 | 详设文档 |
| --- | --- |
| §2, §18 | DD-platform §5 |
| §3 | DD-ingestion §5 |
| §4, §5, §7, §8 | DD-governance §5 |
| §6, §9~11 | DD-asset §5 |
| §17 | DD-research §5 |
| §12~16 | DD-analytics §5 |
| §19 | DD-integration §5 |
