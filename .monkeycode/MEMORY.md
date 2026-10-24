# User Instruction Memory

本文件记录用户指令、偏好与项目运行知识，供后续会话参考。

## Format

### User Instruction Entry

[User Instruction Summary]
- Date: [YYYY-MM-DD]
- Context: [Mentioned scenario or time]
- Instructions:
  - [Content of user teaching or instruction, described line by line]

### Project Knowledge Entry

[Project Knowledge Summary]
- Date: [YYYY-MM-DD]
- Context: Discovered by Agent while performing [specific task description]
- Category: [Operations & Deployment|Build Methods|Testing Methods|Troubleshooting & Debugging|Workflow & Collaboration|Environment Configuration]
- Instructions:
  - [Specific knowledge points, described line by line]

## Entries

[Project Knowledge Summary]
- Date: 2026-10-24
- Context: Discovered by Agent while building the test case management system
- Category: Environment Configuration
- Instructions:
  - 环境未安装 Java/Maven/MySQL，已安装 OpenJDK 17（Oracle 官方下载域名在当前网络不可达）
  - MySQL 8.0.46 通过清华镜像的官方 APT 仓库安装：`deb [trusted=yes] https://mirrors.tuna.tsinghua.edu.cn/mysql/apt/debian bookworm mysql-8.0`
  - 容器内无 systemd，MySQL 需手动启动：`/usr/sbin/mysqld --user=mysql --pid-file=/var/run/mysqld/mysqld.pid --socket=/var/run/mysqld/mysqld.sock`
  - 数据库账号：tcm / Tcm@123456，库名 testcase_db；root 密码 Tcm@123456
  - Maven 依赖走阿里云镜像（写入 /root/.m2/settings.xml），npm 走 https://registry.npmmirror.com
  - 环境内存紧张，Java 服务以 `-Xms96m -Xmx384m` 启动

[Project Knowledge Summary]
- Date: 2026-10-24
- Context: Discovered by Agent while building the test case management system
- Category: Build Methods
- Instructions:
  - 后端构建：`cd /workspace/backend && mvn -DskipTests package`，产物 target/testcase-manager-1.0.0.jar
  - 后端启动：`java -Xms96m -Xmx384m -jar target/testcase-manager-1.0.0.jar`
  - 前端开发：`cd /workspace/frontend && npm run dev`（5173 端口，/api 代理至 8080）
  - 前端构建：`cd /workspace/frontend && npm run build`
