# 开发指南

## 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17 | 开发环境使用 OpenJDK 17.0.20 |
| Maven | 3.8+ | 后端构建 |
| Node.js | 20+ | 前端构建（环境为 Node 22） |
| MySQL | 8.0.46 | 数据库 |

## 快速开始

### 1. 准备数据库

```bash
# 启动 MySQL（容器环境无 systemd 时手动启动）
/usr/sbin/mysqld --user=mysql --pid-file=/var/run/mysqld/mysqld.pid --socket=/var/run/mysqld/mysqld.sock &

# 创建库与账号
mysql -uroot -p < sql/init.sql
```

数据库连接参数见 `backend/src/main/resources/application.yml`，可通过环境变量 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD` 覆盖。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认端口 8080，首次启动自动建表并写入演示数据。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`，默认账号 `admin / admin123`。

## 项目结构说明

- `backend/src/main/java/com/pyq/tcm`：按 `controller / service / repository / entity` 分层
- `frontend/src/views`：页面级组件；`frontend/src/api`：接口封装；`frontend/src/stores`：Pinia 状态
- `.monkeycode/`：需求、设计与项目文档

## 开发规范

### 后端
- 控制器只做参数接收与响应封装，业务逻辑写入 Service
- 所有接口返回 `ApiResponse`，业务异常抛出 `BizException` 并由全局处理器转换
- 实体字段使用驼峰命名，表字段使用下划线命名（Hibernate 默认策略）

### 前端
- 页面组件使用 `<script setup>` 组合式 API
- 接口调用统一经由 `src/api` 中的函数，不在组件内直接写 URL
- 表单校验使用 Element Plus 的 `rules`

### 提交规范
采用 Conventional Commits：`feat:`、`fix:`、`refactor:`、`docs:`、`chore:`。

## 常见任务

### 新增一个业务实体
1. 在 `entity` 中定义实体与字段
2. 在 `repository` 中声明 `JpaRepository`
3. 在 `service` 中实现业务方法
4. 在 `controller` 中暴露 REST 接口
5. 更新 `.monkeycode/docs/INTERFACES.md`

### 调整表结构
开发期由 `spring.jpa.hibernate.ddl-auto=update` 自动同步；生产环境应改用 Flyway 迁移脚本，并同步更新 `sql/schema.sql`。

## 构建与发布

```bash
# 后端打包
cd backend && mvn clean package -DskipTests

# 前端打包
cd frontend && npm run build
```

前端构建产物位于 `frontend/dist`，可交由 Nginx 托管，并将 `/api` 反向代理至后端服务。
