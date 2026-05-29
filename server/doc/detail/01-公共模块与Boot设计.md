# 公共模块与 Boot 设计

> **文档编号**：DD-01  
> **模块**：`nanda-common` · `nanda-boot`  
> **上游**：[08-技术架构推荐](../design/08-技术架构推荐.md)

---

## 1. nanda-common 包结构

```
com.nanda.common/
├── core/
│   ├── result/Result.java, PageResult.java, PageQuery.java
│   ├── exception/BusinessException.java, GlobalExceptionHandler.java
│   └── constant/CommonConstants.java
├── security/
│   ├── jwt/JwtUtils.java, JwtProperties.java
│   ├── context/AuthContext.java, AuthContextHolder.java
│   └── annotation/RequiresPermission.java
├── audit/
│   ├── annotation/AuditLog.java
│   └── aspect/AuditLogAspect.java
├── datascope/
│   ├── annotation/DataScope.java
│   └── interceptor/DataScopeInterceptor.java
├── crypto/
│   ├── annotation/EncryptField.java
│   ├── CryptoService.java, CryptoServiceImpl.java
│   └── EncryptLevel.java (L0/L1/L2/L3)
└── util/IdGenerator.java, JsonUtils.java, DateUtils.java
```

---

## 2. 核心类设计

### 2.1 Result\<T\>

```java
public class Result<T> {
    private int code;           // 0=成功
    private String message;
    private T data;
    private String requestId;
    public static <T> Result<T> ok(T data);
    public static <T> Result<T> fail(int code, String message);
}
```

### 2.2 PageQuery / PageResult

```java
public class PageQuery {
    @Min(1) private int page = 1;
    @Max(100) @Min(1) private int size = 20;
    private String sort;  // "createdAt,desc"
}
public class PageResult<T> {
    private List<T> items;
    private int page, size;
    private long total;
}
```

### 2.3 BusinessException

```java
public class BusinessException extends RuntimeException {
    private final int code;  // 见 03-API与错误码规范
}
```

### 2.4 AuthContext

```java
public class AuthContext {
    private Long userId;
    private String username;
    private Long orgId;           // 当前 X-Org-Id
    private List<Long> orgIds;    // 可访问机构
    private Set<String> permissions;
}
```

### 2.5 CryptoService

```java
public interface CryptoService {
    String encrypt(String plain, EncryptLevel level);
    String decrypt(String cipher, EncryptLevel level);
    String hashForIndex(String plain, String salt);  // id_hash
    String mask(String plain, MaskType type);        // 列表脱敏
}
```

---

## 3. nanda-boot 配置

### 3.1 application.yml 结构

```yaml
spring:
  application:
    name: nanda-server
  datasource:
    url: jdbc:mysql://localhost:3306/nanda?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USER}
    password: ${DB_PASS}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
  redis:
    host: ${REDIS_HOST:localhost}
    port: 6379
  rabbitmq:
    host: ${MQ_HOST:localhost}

nanda:
  jwt:
    secret: ${JWT_SECRET}
    access-expire-seconds: 7200
    refresh-expire-seconds: 604800
  crypto:
    master-key-id: ${MK_ID}

mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

knife4j:
  enable: true
  setting:
    language: zh_cn
```

### 3.2 多环境

| 文件 | 用途 |
| --- | --- |
| application-dev.yml | 本地 MySQL/Redis |
| application-test.yml | 测试环境 |
| application-prod.yml | 生产（密钥外置） |

### 3.3 Security Filter 链顺序

```
1. CorsFilter
2. JwtAuthenticationFilter      → 解析 Bearer Token，填充 SecurityContext
3. OrgContextFilter               → 解析 X-Org-Id，写入 AuthContext
4. UsernamePasswordAuthenticationFilter (login 端点)
5. FilterSecurityInterceptor    → @PreAuthorize
```

### 3.4 启动类

```java
@SpringBootApplication(scanBasePackages = "com.nanda")
@EnableScheduling
public class NandaApplication {
    public static void main(String[] args) {
        SpringApplication.run(NandaApplication.class, args);
    }
}
```

---

## 4. Maven 父 POM 依赖（摘要）

| 依赖 | 版本 |
| --- | --- |
| spring-boot-starter-parent | 2.7.18 |
| mybatis-plus-boot-starter | 3.5.5 |
| mysql-connector-j | 8.0.33 |
| jjwt | 0.11.5 |
| knife4j-spring-boot-starter | 3.0.3 |
| easyexcel | 3.3.4 |
| bcprov-jdk15on | 1.70 |
| flyway-core | 7.15.0 |

---

## 5. Flyway 占位策略（后续实现）

```
db/migration/
├── V1.0.0__init_sys.sql
├── V1.1.0__init_stg.sql
├── V1.2.0__init_gov.sql
├── V1.3.0__init_pub.sql
├── V1.4.0__init_research.sql
├── V1.5.0__init_analytics.sql
└── V1.6.0__init_int.sql
```

DDL 内容见各模块 DD §4 与 [02-数据库设计说明](./02-数据库设计说明.md)。
