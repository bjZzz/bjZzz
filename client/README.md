# Nanda 前端

共病专病科研平台 Web 管理端（Vue 3 + TypeScript + Tailwind CSS + Element Plus）。

## 技术栈

- Vue 3.5 + TypeScript + Vite 6
- Tailwind CSS 4
- Element Plus
- Pinia + Vue Router 4
- Axios（对接 `/api/v1`）

## 本地开发

1. 启动后端依赖（MySQL 等）：

```bash
cd ../server
docker compose up -d
```

2. 启动后端：

```bash
cd ../server
mvn -pl nanda-boot spring-boot:run
```

3. 安装依赖并启动前端：

```bash
npm install
npm run dev
```

访问 http://localhost:5173 ，默认账号 `admin` / `admin123`。

开发环境通过 Vite 代理 `/api` → `http://localhost:8080`，无需后端 CORS。

## 模块结构

| 目录 | 说明 |
| --- | --- |
| `src/api/` | 按后端模块拆分的 API 客户端 |
| `src/views/platform/` | 平台管理（机构/用户/角色/审计） |
| `src/views/ingestion/` | 数据采集 |
| `src/views/governance/` | 数据治理 |
| `src/views/asset/` | 数据资产 |
| `src/views/research/` | 科研协作 |
| `src/views/wave2/` | Wave 2 占位页 |

## Wave 1 范围

- 登录鉴权、权限路由、机构切换
- 平台 / 采集 / 治理 / 资产 / 科研核心 CRUD 页面
- Wave 2 占位：高级检索、导出审批、沙箱 IDE、分中心上传、驾驶舱

## 构建

```bash
npm run build
npm run preview
```

## API 文档

后端 Knife4j：http://localhost:8080/doc.html
