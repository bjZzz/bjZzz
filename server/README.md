# Nanda Server

共病专病科研平台后端（模块化单体 · Spring Boot 2.7.18 · Java 8）。

## 模块结构

| 模块 | 说明 |
| --- | --- |
| nanda-common | 公共组件（Result、JWT、异常、加密） |
| nanda-platform | 平台（认证、机构、用户） |
| nanda-ingestion | 采集 / Staging |
| nanda-governance | 治理 / CRF |
| nanda-asset | 资产 / EMPI / 专病 |
| nanda-research | 科研 / 队列 / 随访 |
| nanda-analytics | 检索 / 分析 / 沙箱 BFF |
| nanda-integration | 外部集成 |
| nanda-boot | 启动入口、Flyway、配置 |

设计文档见 [doc/detail/00-详细设计总览.md](doc/detail/00-详细设计总览.md)。

## 环境要求

- JDK 8
- Maven 3.6+
- Docker（可选，用于 MySQL / Redis）

## Maven 本地仓库

本项目通过 `.mvn/settings.xml` 将本地仓库固定为 **`D:\.m2`**；命令行在项目根目录执行 `mvn` 时会自动加载（见 `.mvn/maven.config`）。

**IntelliJ IDEA**（与命令行一致）：

1. **Settings** → **Build, Execution, Deployment** → **Build Tools** → **Maven**
2. **User settings file**：勾选 **Override**，选择项目内  
   `server/.mvn/settings.xml`
3. **Local repository**：勾选 **Override**，填写 `D:\.m2`
4. 点击 **Reload All Maven Projects**

## 工程目录（代码位置）

| 模块 | Java 根包 | 说明 |
| --- | --- | --- |
| nanda-common | `com.nanda.common.*` | core / security / crypto / util |
| nanda-platform | `com.nanda.platform.{auth,org,user,audit}.*` | 平台域分包 |
| nanda-ingestion | `com.nanda.ingestion.*` | 含 staging、datasource 等子包 |
| nanda-governance | `com.nanda.governance.*` | 含 crf、cleaning 等子包 |
| nanda-asset | `com.nanda.asset.*` | 含 empi、specialty 等子包 |
| nanda-research | `com.nanda.research.*` | project / cohort / followup |
| nanda-analytics | `com.nanda.analytics.*` | search / statistics / report 等 |
| nanda-integration | `com.nanda.integration.*` | upload / fhir / writeback |
| nanda-boot | `com.nanda.NandaApplication` | 启动类、Flyway、配置 |

项目根目录提供 **`mvnw.cmd`**（由 Maven Wrapper 提供，无需单独安装 Maven 到 PATH）。

## 快速启动

```bash
# 1. 启动 MySQL + Redis
docker compose up -d

# 2. 编译打包（Windows 可用 mvnw.cmd）
mvnw.cmd clean package -DskipTests

# 3. 运行（Flyway 自动迁移 V1.0.0~V1.6.0 + 种子数据）
mvnw.cmd -pl nanda-boot -am spring-boot:run -Dspring-boot.run.profiles=dev
```

## 默认账号

| 项 | 值 |
| --- | --- |
| 用户名 | admin |
| 密码 | admin123 |

## API

| 地址 | 说明 |
| --- | --- |
| http://localhost:8080/api/v1/health | 健康检查 |
| http://localhost:8080/api/v1/auth/login | 登录 |
| http://localhost:8080/api/v1/auth/refresh | 刷新令牌（需 Bearer） |
| http://localhost:8080/doc.html | Knife4j 文档 |

登录示例：

```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

## 数据库迁移

脚本位于 `nanda-boot/src/main/resources/db/migration/`：

- V1.0.0 ~ V1.6.0：全库表（sys / stg / gov / pub / res / ana / int）
- V1.0.1：平台种子数据
- V1.0.2：修正 admin 密码哈希（已跑过 V1.0.1 的库）
