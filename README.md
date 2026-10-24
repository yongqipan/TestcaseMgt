# 测试用例管理系统（TCM）

基于 **Java 17 + Spring Boot 3 + Vue 3 + MySQL 8.0.46** 的前后端分离测试用例管理系统。

## 功能

- 账号登录与令牌认证
- 项目 / 模块（多级树）/ 测试用例管理
- 测试计划与逐条执行结果记录
- 用例优先级、状态、执行结果统计
- 工作台质量概览

## 目录结构

```
backend/    Spring Boot REST API
frontend/   Vue 3 + Element Plus 单页应用
sql/        数据库初始化脚本
.monkeycode/ 项目文档与需求设计
```

## 快速开始

### 1. 数据库

```bash
# 启动 MySQL（容器环境无 systemd 时手动启动）
/usr/sbin/mysqld --user=mysql --pid-file=/var/run/mysqld/mysqld.pid --socket=/var/run/mysqld/mysqld.sock &

# 初始化库与账号
mysql -uroot -p < sql/schema.sql
```

### 2. 后端

```bash
cd backend
mvn spring-boot:run
```

服务地址 `http://localhost:8080`，首次启动自动建表并写入演示数据。

### 3. 前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`。

默认账号：`admin / admin123`

## 配置

数据库连接可通过环境变量覆盖：

| 变量 | 默认值 |
|------|--------|
| DB_HOST | 127.0.0.1 |
| DB_PORT | 3306 |
| DB_NAME | testcase_db |
| DB_USER | tcm |
| DB_PASSWORD | Tcm@123456 |

## 文档

- 项目概览：`.monkeycode/docs/INDEX.md`
- 架构设计：`.monkeycode/docs/ARCHITECTURE.md`
- 接口定义：`.monkeycode/docs/INTERFACES.md`
- 开发指南：`.monkeycode/docs/DEVELOPER_GUIDE.md`
